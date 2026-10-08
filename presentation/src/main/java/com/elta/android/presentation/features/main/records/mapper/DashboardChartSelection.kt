package com.elta.android.presentation.features.main.records.mapper

import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.glucoseValue
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseDashboardUiState
import com.elta.android.presentation.features.main.records.ui.compose.GlucosePoint
import java.util.Locale
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

internal fun chartPointsFor(
    selectedDate: LocalDate,
    dashboard: GlucoseDashboardUiState,
    loadedEventsByMonth: Map<YearMonth, List<EventV2>>
): List<GlucosePoint> {
    if (selectedDate == LocalDate.now()) return dashboard.chartPoints
    val glucoseFormat = dashboard.detailedChartData.dailyGlucoseModel?.glucoseFormat ?: return emptyList()
    return loadedEventsByMonth[YearMonth.from(selectedDate)].orEmpty()
        .asSequence()
        .filter { it.type is EventType.Glucose && it.additionTime.toLocalDate() == selectedDate }
        .sortedBy { it.additionTime }
        .map { event ->
            GlucosePoint(
                timeLabel = String.format(Locale.US, "%02d:%02d", event.additionTime.hour, event.additionTime.minute),
                value = event.glucoseValue(glucoseFormat).toFloat()
            )
        }
        .toList()
}

internal fun detailedEventsFor(
    dashboard: GlucoseDashboardUiState,
    loadedEventsByMonth: Map<YearMonth, List<EventV2>>
): List<EventV2> =
    (dashboard.detailedChartData.events + loadedEventsByMonth.values.flatten()).distinct()
