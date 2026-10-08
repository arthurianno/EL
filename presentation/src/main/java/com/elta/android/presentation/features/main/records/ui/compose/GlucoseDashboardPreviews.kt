package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

private val PreviewChartPoints = listOf(
    GlucosePoint("06:00", 5.3f),
    GlucosePoint("09:00", 6.1f),
    GlucosePoint("12:00", 7.4f),
    GlucosePoint("15:00", 5.8f),
    GlucosePoint("18:00", 6.7f)
)

private val PopulatedPreviewState = GlucoseDashboardUiState(
    glucoseValue = "6,7",
    deltaText = "0,5",
    glucoseTrend = GlucoseTrend(GlucoseTrendDirection.UP, "0,5"),
    tirPercentage = "73%",
    syncTimeText = "Сегодня, 10:42",
    breadUnitsText = "2,5 ХЕ",
    insulinText = "4,0 Ед.",
    chartPoints = PreviewChartPoints,
    detailedChartData = DetailedChartData(glucosePoints = PreviewChartPoints.map {
        DetailedGlucosePoint(timeLabel = it.timeLabel, value = it.value)
    })
)

@Preview(name = "Данные — норма", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun GlucoseDashboardNormalPreview() {
    GlucoseDashboardContent(
        uiState = PopulatedPreviewState,
        syncState = DashboardSyncUiState(displayedTime = PopulatedPreviewState.syncTimeText)
    )
}

@Preview(name = "Данные — высокий уровень", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun GlucoseDashboardHighPreview() {
    GlucoseDashboardContent(
        uiState = PopulatedPreviewState.copy(
            glucoseValue = "12,4",
            glucoseState = GlucoseState.HIGH,
            isDarkTheme = true
        ),
        syncState = DashboardSyncUiState(displayedTime = "Только что")
    )
}

@Preview(name = "Данные — низкий уровень", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun GlucoseDashboardLowPreview() {
    GlucoseDashboardContent(
        uiState = PopulatedPreviewState.copy(
            glucoseValue = "3,2",
            glucoseState = GlucoseState.LOW,
            glucoseTrend = GlucoseTrend(GlucoseTrendDirection.DOWN, "0,8")
        ),
        syncState = DashboardSyncUiState(displayedTime = "Сегодня, 10:42")
    )
}

@Preview(name = "Нет измерений", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun GlucoseDashboardEmptyPreview() {
    GlucoseDashboardContent(
        uiState = GlucoseDashboardUiState(),
        syncState = DashboardSyncUiState(displayedTime = "Нет измерений")
    )
}

@Preview(name = "Ошибка синхронизации", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun GlucoseDashboardSyncErrorPreview() {
    GlucoseDashboardContent(
        uiState = PopulatedPreviewState,
        syncState = DashboardSyncUiState(
            displayedTime = "Сегодня, 10:42",
            statusMessage = "Устройство недоступно"
        )
    )
}

@Preview(name = "Синхронизация", widthDp = 360, heightDp = 760, showBackground = true)
@Composable
private fun GlucoseDashboardSyncPreview() {
    GlucoseDashboardContent(
        uiState = PopulatedPreviewState,
        syncState = DashboardSyncUiState(
            displayedTime = "Сегодня, 10:42",
            statusMessage = "Синхронизация с прибором...",
            isSyncing = true
        )
    )
}
