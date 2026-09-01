package com.elta.android.presentation.features.main.records.ui.adapter.items

import android.content.Context
import com.elta.android.domain.features.diary.events.model.glucoseValue
import com.elta.android.presentation.features.main.records.mapper.DetailedChartItemsBuilder
import com.elta.android.presentation.features.main.records.ui.compose.DetailedChartData
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseDashboardUiState
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseState
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseTrend
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseTrendDirection
import com.elta.android.presentation.features.main.records.ui.compose.GlucosePoint
import com.elta.android.presentation.utils.SyncAttemptTimeStore
import java.util.Locale
import kotlin.math.abs

/** Maps the legacy adapter item to a Compose-specific, immutable UI model. */
internal fun RecordsHeaderItem.toGlucoseDashboardUiState(context: Context): GlucoseDashboardUiState {
    val glucoseValue = glucoseLevel?.format()?.takeIf { it.isNotBlank() } ?: "—"
    val numericValue = glucoseValue.replace(',', '.').toFloatOrNull()
    val model = dailyGlucoseModel
    val detailedPoints = model?.let { DetailedChartItemsBuilder.buildPoints(it, allEvents) }.orEmpty()

    return GlucoseDashboardUiState(
        glucoseValue = glucoseValue,
        deltaText = glucoseLevelIndex?.format()?.takeIf { it.isNotBlank() } ?: "—",
        glucoseTrend = calculateGlucoseTrend(),
        tirPercentage = calculateTimeInRange(),
        syncTimeText = SyncAttemptTimeStore.getLastAttemptText(context),
        breadUnitsText = breadLevel?.let { "$it ХЕ" } ?: "0,0 ХЕ",
        insulinText = insulinLevel?.let { "$it Ед." } ?: "0,0 Ед.",
        glucoseState = resolveGlucoseState(numericValue),
        chartPoints = detailedPoints.map { GlucosePoint(it.timeLabel, it.value) },
        detailedChartData = DetailedChartData(
            glucosePoints = detailedPoints,
            insulinEntries = DetailedChartItemsBuilder.buildInsulinEntries(detailedPoints, allEvents),
            foodEntries = DetailedChartItemsBuilder.buildFoodEntries(detailedPoints, allEvents),
            activityEntries = DetailedChartItemsBuilder.buildActivityEntries(allEvents),
            dailyGlucoseModel = model,
            events = allEvents
        )
    )
}

private fun RecordsHeaderItem.resolveGlucoseState(value: Float?): GlucoseState {
    val settings = dailyGlucoseModel?.glucoseLevelSettings
    return when {
        value == null -> GlucoseState.NORMAL
        settings != null && value.toDouble() in settings.low -> GlucoseState.LOW
        settings != null && value.toDouble() in settings.high -> GlucoseState.HIGH
        settings == null && value < 3.9f -> GlucoseState.LOW
        settings == null && value > 10f -> GlucoseState.HIGH
        else -> GlucoseState.NORMAL
    }
}

private fun RecordsHeaderItem.calculateTimeInRange(): String {
    val model = dailyGlucoseModel ?: return "—"
    val values = model.glucoseEvents.map { it.glucoseValue(model.glucoseFormat) }
    return values.takeIf { it.size >= 2 }
        ?.let { values.count { value -> value in model.glucoseLevelSettings.normal } * 100 / it.size }
        ?.let { "$it%" }
        ?: "—"
}

private fun RecordsHeaderItem.calculateGlucoseTrend(): GlucoseTrend? {
    val model = dailyGlucoseModel ?: return null
    val events = model.glucoseEvents.sortedBy { it.additionTime }
    if (events.size < 2) return null

    val lastIndex = events.lastIndex
    val difference = events[lastIndex].glucoseValue(model.glucoseFormat) -
        events[lastIndex - 1].glucoseValue(model.glucoseFormat)

    return GlucoseTrend(
        direction = when {
            difference > 0.0 -> GlucoseTrendDirection.UP
            difference < 0.0 -> GlucoseTrendDirection.DOWN
            else -> GlucoseTrendDirection.STABLE
        },
        valueText = String.format(Locale.US, "%.1f", abs(difference)).replace('.', ',')
    )
}
