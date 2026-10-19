package com.elta.android.presentation.features.statistic.period.ui.compose

import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.elta.android.presentation.features.main.records.ui.compose.*
import com.elta.android.presentation.features.statistic.period.ui.Period
import com.elta.android.presentation.features.statistic.period.model.DEFAULT_VISIBLE_STATISTICS_BLOCKS
import com.elta.android.presentation.features.statistic.period.model.StatisticsBlock
import com.elta.android.presentation.theme.EltaTheme
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StatisticsDashboardScreenTest {
    @get:Rule val compose = createAndroidComposeRule<StatisticsDashboardTestActivity>()

    @Test fun referenceScreenKeepsFullSizedChartsAndReferenceOrder() {
        showScreen(height = 2100)
        compose.onNodeWithText("4,1").assertIsDisplayed()
        compose.onNodeWithText("Последние 14 дней").assertIsDisplayed()
        val titles = listOf("Основные показатели за период", "Суточные колебания",
            "Статистика гипо/гипергликемии", "Ключевые метрики", "Сравнение с предыдущим периодом")
        val sections = titles.map { compose.onNodeWithText(it).assertIsDisplayed().fetchSemanticsNode().boundsInRoot }
        sections.zipWithNext().forEach { (a, b) -> assertTrue(b.top > a.bottom) }
        saveScreenshot("reference", compose.onNodeWithTag("statistics_screen"))
    }

    @Test fun compactScreenKeepsHeaderAndSettingsReachableAndSavesVisibility() {
        var saved: List<StatisticsBlock>? = null
        showScreen(width = 320, height = 720, onSave = { saved = it })
        compose.onNodeWithText("Статистика").assertIsDisplayed()
        compose.onNodeWithContentDescription("Выгрузить статистику").assertIsDisplayed()
        saveScreenshot("compact", compose.onNodeWithTag("statistics_screen"))
        compose.onNodeWithContentDescription("Настройки статистики").performClick()
        compose.onNodeWithText("Настройка статистики").assertIsDisplayed()
        compose.onNodeWithContentDescription("Скрыть Суточные колебания").performClick()
        compose.onNodeWithContentDescription("Показать Активность").performClick()
        saveScreenshot("settings", compose.onNode(isDialog()))
        compose.onNodeWithText("Сохранить изменения").performClick()
        compose.runOnIdle {
            assertEquals(DEFAULT_VISIBLE_STATISTICS_BLOCKS - StatisticsBlock.DAILY + StatisticsBlock.ACTIVITY, saved)
        }
        compose.onNodeWithContentDescription("Настройки статистики").performClick()
        compose.onNodeWithContentDescription("Показать Суточные колебания").assertExists()
        compose.onNodeWithContentDescription("Скрыть Активность").assertExists()
    }

    @Test fun longMonthUsesCompactDateWithoutTruncatingIt() {
        showScreen(width = 361, height = 782, state = StatisticsDashboardPreviewFixture.state.copy(
            periodTitle = "13 октября – 19 октября", periodCompactTitle = "13 окт. – 19 окт."
        ))
        compose.onNodeWithText("13 окт. – 19 окт.").assertIsDisplayed()
        saveScreenshot("long-month", compose.onNodeWithTag("statistics_screen"))
    }

    @Test fun additionalSectionsDisplayFoodAndActivityData() {
        val base = StatisticsDashboardPreviewFixture.state
        val state = base.copy(
            activity = ActivityStatisticsUiState(
                days = base.hourlyRanges.mapIndexed { index, day -> ActivityDay(day.date, 20.0 + index * 5, 4.0 + index % 5) },
                averageMinutes = 52.5
            ),
            nutrition = NutritionStatisticsUiState(1457.0, 47.0, 78.0, 211.0, 18.4, listOf(
                MealGlucoseUiState("Завтрак", 5.2, 7.8), MealGlucoseUiState("Обед", 6.1, 7.0),
                MealGlucoseUiState("Перекус", 6.3, 9.4), MealGlucoseUiState("Ужин", 4.8, 5.6)
            ))
        )
        showScreen(height = 1800, state = state, initialBlocks = listOf(StatisticsBlock.ACTIVITY, StatisticsBlock.FOOD))
        compose.onAllNodesWithText("Активность").onFirst().assertIsDisplayed()
        compose.onNodeWithText("Питание").assertIsDisplayed()
        compose.onNodeWithText("18,4 ХЕ").assertIsDisplayed()
        compose.onNodeWithText("1457").assertIsDisplayed()
        saveScreenshot("additional-sections", compose.onNodeWithTag("statistics_screen"))
    }

    @Test fun missingAndZeroStatisticsUseRoundedDashes() {
        showScreen(height = 1400, state = StatisticsDashboardUiState(Period.SEVEN, "13 октября – 19 октября", periodCompactTitle = "13 окт. – 19 окт."))
        compose.onNodeWithText("0%").assertDoesNotExist()
        compose.onNodeWithText("0 изм.").assertDoesNotExist()
        compose.onAllNodesWithContentDescription("Нет данных").onFirst().assertIsDisplayed()
        saveScreenshot("empty-values", compose.onNodeWithTag("statistics_screen"))
    }

    @Test fun zeroRangesUseDashesAndPositiveValuesStayVisible() {
        showScreen(state = StatisticsDashboardPreviewFixture.state.copy(
            lowPercent = 0, lowCount = 0, highPercent = 0, highCount = 0,
            inRangePercent = 100, inRangeCount = 5
        ))
        compose.onNodeWithText("0%").assertDoesNotExist()
        compose.onNodeWithText("0 изм.").assertDoesNotExist()
        compose.onNodeWithText("100%").assertIsDisplayed()
        compose.onNodeWithText("5 изм.").assertIsDisplayed()
    }

    @Test fun dashboardUsesTheSameSymbolForLegacyEmptyTirAndZeroTotals() {
        compose.setContent {
            EltaTheme {
                Box(Modifier.fillMaxSize().testTag("dashboard_screen")) {
                    GlucoseDashboardContent(
                        uiState = GlucoseDashboardUiState(
                            glucoseValue = "4,1", glucoseValueNumber = 4.1,
                            tirPercentage = "— –", tirPercent = null,
                            breadUnitsText = "0,0 ХЕ", insulinText = "0,0 Ед."
                        ),
                        syncState = DashboardSyncUiState(displayedTime = ""), onAction = {}
                    )
                }
            }
        }
        compose.onNodeWithText("4,1").assertIsDisplayed()
        compose.onNodeWithText("TIR").assertIsDisplayed()
        compose.onNodeWithText("— –").assertDoesNotExist()
        compose.onNodeWithText("0,0 ХЕ").assertDoesNotExist()
        compose.onNodeWithText("0,0 Ед.").assertDoesNotExist()
        saveScreenshot("dashboard-empty-metrics", compose.onNodeWithTag("dashboard_screen"))
    }

    @Test fun periodAndExportActionsRemainFunctional() {
        var exports = 0
        var period = 0
        showScreen(onExport = { exports++ }, onPeriod = { period = it })
        compose.onNodeWithContentDescription("Выгрузить статистику").performClick()
        compose.onNodeWithText("Последние 14 дней").performClick()
        compose.onNodeWithText("Последние 7 дней").performClick()
        compose.runOnIdle { assertEquals(1, exports); assertEquals(7, period) }
    }

    private fun showScreen(
        width: Int = 375, height: Int = 812,
        state: StatisticsDashboardUiState = StatisticsDashboardPreviewFixture.state,
        initialBlocks: List<StatisticsBlock> = DEFAULT_VISIBLE_STATISTICS_BLOCKS,
        onSave: (List<StatisticsBlock>) -> Unit = {},
        onExport: () -> Unit = {}, onPeriod: (Int) -> Unit = {}
    ) {
        compose.setContent {
            EltaTheme {
                BoxWithConstraints(Modifier.fillMaxSize()) {
                    val density = LocalDensity.current.density
                    val scale = minOf(maxWidth.value / width, maxHeight.value / height)
                    CompositionLocalProvider(LocalDensity provides Density(density * scale)) {
                        Box(Modifier.size(width.dp, height.dp).align(Alignment.TopCenter).testTag("statistics_screen")) {
                            var blocks by remember { mutableStateOf(initialBlocks) }
                            StatisticsDashboardScreen(
                                uiState = state, visibleBlocks = blocks,
                                onVisibleBlocksSaved = { blocks = it; onSave(it) },
                                onPeriodSelected = { onPeriod(it.count) }, onBack = {}, onExport = onExport,
                                windowInsets = WindowInsets(0.dp, 44.dp, 0.dp, 34.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun saveScreenshot(name: String, node: SemanticsNodeInteraction) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "statistics-$name.png")
        file.outputStream().use { node.captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
        if (InstrumentationRegistry.getArguments().getString("exportStatisticsScreenshots") == "true") {
            ParcelFileDescriptor.AutoCloseInputStream(instrumentation.uiAutomation.executeShellCommand(
                "cp ${file.absolutePath} /sdcard/Download/elta-statistics-$name.png"
            )).use { it.readBytes() }
        }
    }
}

class StatisticsDashboardTestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
