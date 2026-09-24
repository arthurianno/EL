package com.elta.android.presentation.features.main.records.ui.compose

import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.home.model.DailyGlucoseModel

/** Immutable input for the dashboard UI. */
data class GlucoseDashboardUiState(
    val device: DashboardDevice? = null,
    val sensor: DashboardSensor? = null,
    val glucoseValue: String = "—",
    val deltaText: String = "—",
    val glucoseTrend: GlucoseTrend? = null,
    val tirPercentage: String = "—",
    val syncTimeText: String = "Нет измерений",
    val breadUnitsText: String = "0,0 ХЕ",
    val insulinText: String = "0,0 Ед.",
    val glucoseState: GlucoseState = GlucoseState.NORMAL,
    val isDarkTheme: Boolean = false,
    val chartPoints: List<GlucosePoint> = emptyList(),
    val detailedChartData: DetailedChartData = DetailedChartData()
) {
    val hasMeasurements: Boolean
        get() = detailedChartData.dailyGlucoseModel?.hasEvents == true ||
            glucoseValue.replace(',', '.').toFloatOrNull() != null
}

/** Data required by the full-screen chart. It is kept separate from the dashboard summary. */
data class DetailedChartData(
    val glucosePoints: List<DetailedGlucosePoint> = emptyList(),
    val insulinEntries: List<DetailedInsulinEntry> = emptyList(),
    val foodEntries: List<DetailedFoodEntry> = emptyList(),
    val activityEntries: List<DetailedActivityEntry> = emptyList(),
    val dailyGlucoseModel: DailyGlucoseModel? = null,
    val events: List<EventV2> = emptyList()
)

data class DashboardSyncUiState(
    val displayedTime: String,
    val statusMessage: String? = null,
    val isSyncing: Boolean = false,
    val isError: Boolean = false,
    val retryTarget: DashboardSyncTarget = DashboardSyncTarget.METER
)

sealed class GlucoseDashboardAction {
    data object RequestSync : GlucoseDashboardAction()
    data object ConnectDevice : GlucoseDashboardAction()
    data object DismissSyncMessage : GlucoseDashboardAction()
    data class RetrySync(val target: DashboardSyncTarget) : GlucoseDashboardAction()
    data class OpenDevice(val device: DashboardDevice) : GlucoseDashboardAction()
    data class SelectCategory(val category: String) : GlucoseDashboardAction()
    data class RequestDetailedRange(
        val start: org.threeten.bp.LocalDate,
        val end: org.threeten.bp.LocalDate
    ) : GlucoseDashboardAction()
}

/** The primary paired meter; Bluetooth availability is checked when synchronization starts. */
data class DashboardDevice(
    val address: String,
    val name: String,
    val serialNumber: String?,
    val lastSyncAtMillis: Long?
)

data class DashboardSensor(
    val id: String,
    val remainingPercent: Int,
    val remainingDays: Int,
    val isActive: Boolean,
    val lastSyncAtMillis: Long? = null
)

enum class DashboardSyncTarget { METER, SERVER }
