package com.elta.android.presentation.features.main.records.ui.compose

import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.elta.android.presentation.theme.EltaTheme
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DashboardAdaptiveLayoutTest {
    @get:Rule val compose = createAndroidComposeRule<DashboardAdaptiveTestActivity>()

    @Test fun readingFitsAcrossPhoneSizesAndLargeValues() {
        val cases = listOf(
            Fixture(320, 520, "2,1"), Fixture(320, 644, "10,0"),
            Fixture(360, 748, "2,1"), Fixture(360, 748, "33,3"),
            Fixture(393, 733, "2,1"), Fixture(430, 820, "10,0"),
            Fixture(320, 600, "2,1", 1.3f), Fixture(360, 748, "33,3", 1.3f)
        )
        val current = mutableStateOf(cases.first())
        showDashboard(current)
        cases.forEach { fixture ->
            compose.runOnIdle { current.value = fixture }
            val disc = compose.onNodeWithTag("glucose_gauge_reading").fetchSemanticsNode().boundsInRoot
            val value = compose.onNodeWithTag("glucose_gauge_value").assertIsDisplayed()
            val unit = compose.onNodeWithTag("glucose_gauge_unit").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            val trend = compose.onNodeWithTag("glucose_gauge_trend", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
            val bounds = value.fetchSemanticsNode().boundsInRoot
            val label = fixture.toString()
            assertTrue("$label: number and unit do not overlap", bounds.bottom <= unit.top)
            assertTrue("$label: unit and trend do not overlap", unit.bottom <= trend.top)
            listOf(bounds, unit, trend).forEach {
                assertTrue("$label: reading stays inside the disc", it.left >= disc.left && it.right <= disc.right && it.top >= disc.top && it.bottom <= disc.bottom)
            }
            val layouts = mutableListOf<TextLayoutResult>()
            value.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            if (fixture == cases.first()) saveScreenshot("compact-320")
            val textLayout = layouts.single()
            assertFalse("$label: number is not clipped; size=${textLayout.size}; constraints=${textLayout.layoutInput.constraints}; font=${textLayout.layoutInput.style.fontSize}; widthOverflow=${textLayout.didOverflowWidth}; heightOverflow=${textLayout.didOverflowHeight}", textLayout.hasVisualOverflow)
            if (fixture == cases[2]) saveScreenshot("tall-360")
            if (fixture == cases.last()) saveScreenshot("large-text")
        }
    }

    @Test fun todayIsReadOnlyAndPeriodButtonsRemainAvailable() {
        showDashboard(mutableStateOf(Fixture(360, 748, "2,1")))
        val today = compose.onNodeWithText("Сегодня").assertIsDisplayed().assertHasNoClickAction()
        today.performTouchInput { click() }
        compose.onNode(isDialog()).assertDoesNotExist()
        compose.onNodeWithText("12 ч").assertHasClickAction().performClick()
        compose.onNode(isDialog()).assertDoesNotExist()
        compose.onNodeWithText("Сегодня").assertIsDisplayed()
    }

    @Test fun accessibilityTextKeepsReadingVisible() {
        showDashboard(mutableStateOf(Fixture(320, 600, "10,0", 1.5f)))
        compose.onNodeWithText("10,0").assertIsDisplayed()
        compose.onNodeWithText("ммоль/л").assertIsDisplayed()
        compose.onNodeWithTag("glucose_gauge_reading").assertDoesNotExist()
    }

    private fun showDashboard(current: androidx.compose.runtime.MutableState<Fixture>) {
        compose.setContent {
            EltaTheme {
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    val fixture = current.value
                    val density = LocalDensity.current.density
                    val scale = minOf(maxWidth.value / fixture.width, maxHeight.value / fixture.height)
                    CompositionLocalProvider(LocalDensity provides Density(density * scale, fixture.fontScale)) {
                        Box(Modifier.size(fixture.width.dp, fixture.height.dp).align(Alignment.TopCenter).testTag("dashboard_adaptive")) {
                            GlucoseDashboardContent(
                                uiState = GlucoseDashboardUiState(
                                    glucoseValue = fixture.value,
                                    glucoseValueNumber = fixture.value.replace(',', '.').toDouble(),
                                    glucoseState = if (fixture.value == "2,1") GlucoseState.LOW else GlucoseState.HIGH,
                                    glucoseTrend = GlucoseTrend(GlucoseTrendDirection.DOWN, "0,9"),
                                    tirPercentage = "33%", tirPercent = 33,
                                    breadUnitsText = "7,0 ХЕ", insulinText = "7,0 Ед.",
                                    chartPoints = listOf(GlucosePoint("14:00", 11f), GlucosePoint("15:00", 2.1f))
                                ),
                                syncState = DashboardSyncUiState(displayedTime = ""), onAction = {}
                            )
                        }
                    }
                }
            }
        }
    }

    private fun saveScreenshot(name: String) {
        if (InstrumentationRegistry.getArguments().getString("exportDashboardScreenshots") != "true") return
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "dashboard-$name.png")
        file.outputStream().use {
            compose.onNodeWithTag("dashboard_adaptive").captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(
            "cp ${file.absolutePath} /sdcard/Download/elta-dashboard-$name.png"
        )).use { it.readBytes() }
    }

    private data class Fixture(val width: Int, val height: Int, val value: String, val fontScale: Float = 1f)
}

class DashboardAdaptiveTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
