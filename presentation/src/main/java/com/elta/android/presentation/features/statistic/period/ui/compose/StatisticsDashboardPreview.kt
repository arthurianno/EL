package com.elta.android.presentation.features.statistic.period.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.elta.android.presentation.features.statistic.period.ui.Period

/** Preview uses deterministic fixture data and has no dependency on the Android runtime. */
@Preview(
    name = "Статистика · iPhone reference",
    showBackground = true,
    widthDp = 375,
    heightDp = 812
)
@Composable
private fun StatisticsDashboardPreview() {
    StatisticsDashboardScreen(
        uiState = StatisticsDashboardPreviewFixture.state,
        onPeriodSelected = {},
        onBack = {},
        onSettingsClick = {}
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
        distribution = listOf(8, 33, 68, 59, 42, 18)
    )

    private fun hourlyRange(day: Int) = HourlyRange(
        dayLabel = day.toString(),
        statuses = (0..23).map { hour ->
            when {
                hour in 2..4 && day % 4 == 0 -> HourlyRangeStatus.LOW
                hour in 12..14 && day % 5 == 0 -> HourlyRangeStatus.HIGH
                else -> HourlyRangeStatus.IN_RANGE
            }
        }
    )
}
