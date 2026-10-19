package com.elta.android.presentation.features.main.records.ui.compose

import android.content.Context
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.platform.app.InstrumentationRegistry
import com.elta.android.presentation.R
import com.elta.android.presentation.theme.EltaTheme
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate

class ContinuousChartScreenTest {
    @get:Rule val compose = createAndroidComposeRule<ContinuousChartTestActivity>()

    @Before
    fun resetChartPreferences() {
        compose.activity.getSharedPreferences("glucose_chart_preferences", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun chartSummaryAndHintFitWithoutScrolling() {
        showChart()
        listOf("Инсулин", "Еда", "Активность").forEach {
            compose.onNodeWithContentDescription(it).assertIsDisplayed()
        }
        compose.onNodeWithText("GMI").assertIsDisplayed()
        val gmi = continuousFormat(ContinuousStatistics.from(points, null).gmi) + "%"
        val metric = compose.onNodeWithText(gmi).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val dialog = compose.onNode(isDialog()).fetchSemanticsNode().boundsInRoot
        assertTrue("GMI value is fully inside the screen", metric.bottom <= dialog.bottom)
        assertTrue("No vertical scroll is needed", compose.onAllNodes(hasScrollAction()).fetchSemanticsNodes().isEmpty())
        val hint = compose.onNodeWithText(compose.activity.getString(R.string.glucose_chart_zoom_hint))
            .assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val plot = compose.onNodeWithTag("continuous_chart_plot").fetchSemanticsNode().boundsInRoot
        assertTrue("Hint does not cover the graph", hint.bottom <= plot.top)
        val density = compose.activity.resources.displayMetrics.density
        assertTrue("Plot keeps its readable design height", plot.height / density - 12f >= 110f)
        listOf("Инсулин", "Еда", "Активность").forEach {
            val filter = compose.onNodeWithContentDescription(it).fetchSemanticsNode().boundsInRoot
            assertTrue("Filters stay to the right of the plot", filter.left >= plot.right)
            assertTrue("Filters stay inside the chart", filter.top >= plot.top && filter.bottom <= plot.bottom)
        }
        saveScreenshot("overview")

        compose.onNodeWithContentDescription(compose.activity.getString(R.string.glucose_chart_zoom_hint_dismiss))
            .performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.glucose_chart_zoom_hint)).assertDoesNotExist()
        assertFalse(ContinuousChartPreferences(compose.activity).shouldShowZoomHint())
    }

    @Test
    fun firstPinchDismissesHintAndKeepsItDismissedOnReopen() {
        val visible = mutableStateOf(true)
        showChart(visible)
        compose.onNodeWithTag("continuous_chart_plot").performTouchInput {
            val y = center.y
            down(0, Offset(width * 0.4f, y))
            down(1, Offset(width * 0.6f, y))
            repeat(10) { index ->
                moveTo(0, Offset(width * (0.4f - (index + 1) * 0.02f), y))
                moveTo(1, Offset(width * (0.6f + (index + 1) * 0.02f), y))
            }
            up(0)
            up(1)
        }
        compose.onNodeWithText(compose.activity.getString(R.string.glucose_chart_zoom_hint)).assertDoesNotExist()
        compose.runOnIdle { visible.value = false }
        compose.waitForIdle()
        compose.runOnIdle { visible.value = true }
        compose.onNodeWithText("GMI").assertIsDisplayed()
        compose.onNodeWithText(compose.activity.getString(R.string.glucose_chart_zoom_hint)).assertDoesNotExist()
    }

    @Test
    fun glucoseAndAllEventTypesCanBeSelected() {
        showChart()
        val density = compose.activity.resources.displayMetrics.density
        val plot = compose.onNodeWithTag("continuous_chart_plot")
        plot.performTouchInput {
            val chartHeight = height - 12f * density
            click(Offset(width * 10f / 24f, chartHeight * (1f - 7f / 16f)))
        }
        compose.onNodeWithText("Глюкоза").assertIsDisplayed()
        compose.onNodeWithText("7,0").assertIsDisplayed()
        saveScreenshot("selected-glucose")

        listOf(
            Triple(11f, 0.25f, "Инсулин"),
            Triple(11.5f, 0.125f, "Еда"),
            Triple(14.25f, 0f, "Активность")
        ).forEach { (hour, heightRatio, title) ->
            plot.performTouchInput {
                val chartHeight = height - 12f * density
                val y = if (heightRatio == 0f) chartHeight + 6f * density
                    else chartHeight * (1f - heightRatio) - 10f * density
                click(Offset(width * hour / 24f, y))
            }
            compose.onNodeWithText("Событие").assertIsDisplayed()
            compose.onNodeWithText(title).assertIsDisplayed()
        }
        listOf("Инсулин", "Еда", "Активность").forEach {
            compose.onNodeWithContentDescription(it).performClick()
        }
        plot.performTouchInput {
            click(Offset(width * 10f / 24f, height * (1f - 7f / 16f)))
        }
        compose.onNodeWithText("Глюкоза").assertIsDisplayed()
        compose.onNodeWithText("7,0").assertIsDisplayed()
        val bitmap = plot.captureToImage().asAndroidBitmap()
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        assertTrue("Hiding events preserves glucose colors", pixels.any { it == ContinuousNormal.toArgb() })
    }

    private fun showChart(visible: androidx.compose.runtime.MutableState<Boolean> = mutableStateOf(true)) {
        compose.setContent {
            EltaTheme {
                if (visible.value) {
                    DetailedGlucoseChartScreen(
                        glucosePoints = points,
                        insulinEntries = listOf(DetailedInsulinEntry("11:00", 1, "4 Ед.", 0.25f, date, 4f)),
                        foodEntries = listOf(DetailedFoodEntry("11:30", 1, "2 ХЕ", 0.125f, date, 2f)),
                        activityEntries = listOf(DetailedActivityEntry("14:00", "14:30", 30, date, date))
                    )
                }
            }
        }
        compose.waitForIdle()
    }

    private fun saveScreenshot(name: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val directory = File(instrumentation.targetContext.getExternalFilesDir(null), "chart-audit")
        directory.mkdirs()
        val screenshot = File(directory, "$name.png")
        screenshot.outputStream().use {
            compose.onNode(isDialog()).captureToImage().asAndroidBitmap()
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        if (InstrumentationRegistry.getArguments().getString("exportChartScreenshots") == "true") {
            ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(
                "cp ${screenshot.absolutePath} /sdcard/Download/elta-chart-$name.png"
            )).use { it.readBytes() }
        }
    }

    private val date = LocalDate.now()
    private val points = listOf(
        DetailedGlucosePoint("08:00", 3f, date),
        DetailedGlucosePoint("10:00", 7f, date),
        DetailedGlucosePoint("12:00", 11f, date),
        DetailedGlucosePoint("15:00", 6f, date)
    )
}

class ContinuousChartTestActivity : ComponentActivity()
