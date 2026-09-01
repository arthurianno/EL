package com.elta.android.presentation.features.main.records.ui.compose

import androidx.compose.runtime.Composable
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.home.model.DailyGlucoseModel
import org.threeten.bp.LocalDate

data class DetailedGlucosePoint(
    val timeLabel: String,
    val value: Float,
    val date: LocalDate? = null,
    val isMin: Boolean = false,
    val isMax: Boolean = false,
    val trendText: String = "стабилен",
    val trendValue: String = "0,0",
    val foodTimeAgo: String? = null,
    val foodUnits: String? = null,
    val insulinTimeAgo: String? = null,
    val insulinUnits: String? = null,
    val activityTimeAgo: String? = null,
    val activityDuration: String? = null
)

data class DetailedInsulinEntry(
    val timeLabel: String,
    val xIndex: Int,
    val units: String,
    val heightRatio: Float,
    val date: LocalDate? = null,
    val value: Float? = null
)

data class DetailedFoodEntry(
    val timeLabel: String,
    val xIndex: Int,
    val breadUnits: String,
    val heightRatio: Float,
    val date: LocalDate? = null,
    val value: Float? = null
)

data class DetailedActivityEntry(
    val startTimeLabel: String,
    val endTimeLabel: String,
    val durationMins: Long,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)

fun getTodayFormattedDate(): String {
    val now = LocalDate.now()
    val months = listOf(
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
    )
    return "${now.dayOfMonth} ${months[now.monthValue - 1]} ${now.year}"
}

/**
 * Compatibility entry point for the detailed chart. The obsolete landscape implementation was
 * removed: [ContinuousDetailedGlucoseChartScreen] is the sole production implementation.
 */
@Composable
fun DetailedGlucoseChartScreen(
    onBackClick: () -> Unit = {},
    initialDate: String = getTodayFormattedDate(),
    glucosePoints: List<DetailedGlucosePoint> = emptyList(),
    insulinEntries: List<DetailedInsulinEntry> = emptyList(),
    foodEntries: List<DetailedFoodEntry> = emptyList(),
    activityEntries: List<DetailedActivityEntry> = emptyList(),
    dailyGlucoseModel: DailyGlucoseModel? = null,
    allEvents: List<EventV2> = emptyList(),
    onDateRangeSelected: (LocalDate, LocalDate) -> Unit = { _, _ -> }
) {
    ContinuousDetailedGlucoseChartScreen(
        onBackClick = onBackClick,
        initialDate = initialDate,
        fallbackGlucosePoints = glucosePoints,
        fallbackInsulinEntries = insulinEntries,
        fallbackFoodEntries = foodEntries,
        fallbackActivityEntries = activityEntries,
        dailyGlucoseModel = dailyGlucoseModel,
        allEvents = allEvents,
        onMonthsNeeded = onDateRangeSelected
    )
}
