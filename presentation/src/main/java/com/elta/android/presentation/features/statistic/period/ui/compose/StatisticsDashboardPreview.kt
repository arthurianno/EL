package com.elta.android.presentation.features.statistic.period.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.elta.android.presentation.features.statistic.period.ui.Period
import org.threeten.bp.LocalDate

/** Preview uses deterministic fixture data and has no dependency on the Android runtime. */
@Preview(
    name = "Статистика · reference 375dp",
    showBackground = true,
    widthDp = 375,
    heightDp = 812
)
@Composable
private fun StatisticsDashboardReferencePreview() {
    StatisticsDashboardPreviewContent()
}

@Preview(
    name = "Статистика · compact 320dp",
    showBackground = true,
    widthDp = 320,
    heightDp = 720
)
@Composable
private fun StatisticsDashboardCompactPreview() {
    StatisticsDashboardPreviewContent()
}

@Preview(
    name = "Статистика · wide 480dp",
    showBackground = true,
    widthDp = 480,
    heightDp = 900
)
@Composable
private fun StatisticsDashboardWidePreview() {
    StatisticsDashboardPreviewContent()
}

@Composable
private fun StatisticsDashboardPreviewContent() {
    StatisticsDashboardScreen(
        uiState = StatisticsDashboardPreviewFixture.state,
        onPeriodSelected = {},
        onBack = {}
    )
}

private object StatisticsDashboardPreviewFixture {
    val state = StatisticsDashboardUiState(
        period = Period.FOURTEEN,
        periodTitle = "15 июля – 28 июля",
        average = "4,1",
        lowPercent = 5,
        inRangePercent = 78,
        highPercent = 17,
        lowCount = 14,
        inRangeCount = 214,
        highCount = 48,
        minLabel = "1,7",
        maxLabel = "15,8",
        coefficientOfVariation = "31%",
        standardDeviation = "2,1",
        gmi = "6,7",
        nightHypoEpisodes = 4,
        hourlyRanges = (15..28).map(::hourlyRange),
        dailyRangeTitle = "22 июля – 28 июля",
        distribution = listOf(28, 190, 520, 450, 320, 150),
        dailyEpisodes = (15..28).map { day ->
            DailyEpisodeCount(
                date = LocalDate.of(2026, 7, day),
                low = if (day in listOf(13, 16, 18)) 1 else 0,
                high = if (day in listOf(12, 14, 19, 24)) 1 else 0
            )
        },
        comparison = ComparisonUiState(
            currentTir = 78,
            previousTir = 83,
            currentAverage = "6,8",
            previousAverage = "7,2",
            currentAverageValue = 6.8,
            previousAverageValue = 7.2,
            normalStart = 3.9,
            normalEnd = 10.0,
            currentHypoEpisodes = 4,
            previousHypoEpisodes = 6,
            axisDates = (15..28).map { LocalDate.of(2026, 7, it) },
            currentSeries = (15..28).map { day ->
                DailyGlucosePoint(
                    date = LocalDate.of(2026, 7, day),
                    value = 5.5 + (day % 5) * 0.7,
                    position = (day - 15) / 14f
                )
            },
            previousSeries = (15..28).map { day ->
                DailyGlucosePoint(
                    date = LocalDate.of(2026, 7, day),
                    value = 6.2 + (day % 4) * 0.5,
                    position = (day - 15) / 14f
                )
            }
        )
    )

    private fun hourlyRange(day: Int) = HourlyRange(
        date = LocalDate.of(2026, 7, day),
        dayLabel = listOf("ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")[(day - 15) % 7],
        statuses = (0..23).map { hour ->
            when {
                hour in 2..4 && day % 4 == 0 -> HourlyRangeStatus.LOW
                hour in 12..14 && day % 5 == 0 -> HourlyRangeStatus.HIGH
                else -> HourlyRangeStatus.IN_RANGE
            }
        }
    )
}
