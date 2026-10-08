package com.elta.android.presentation.features.main.records.mapper

import com.elta.android.domain.features.diary.events.model.glucoseValue
import com.elta.android.domain.features.diary.home.model.DoubleRange
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.presentation.features.main.records.ui.compose.DashboardDevice
import com.elta.android.presentation.features.main.records.ui.compose.DetailedChartData
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseDashboardUiState
import com.elta.android.presentation.features.main.records.ui.compose.GlucosePoint
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseState
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseTrend
import com.elta.android.presentation.features.main.records.ui.compose.GlucoseTrendDirection
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

/** Converts the home use case result into the state rendered by the dashboard. */
class MainRecordsMapper @Inject constructor() {

    fun map(model: HomeModel, device: DashboardDevice?): GlucoseDashboardUiState {
        val numberFormat = DecimalFormat("#.#").apply {
            minimumFractionDigits = 1
            decimalFormatSymbols = DecimalFormatSymbols().apply { decimalSeparator = ',' }
        }
        val dailyModel = model.dailyGlucoseModel
        val events = model.eventsBlocks.flatMap { it.events }
        val detailedPoints = DetailedChartItemsBuilder.buildPoints(dailyModel, events)
        val glucoseValue = model.lastGlucoseEvent?.value.format(numberFormat)?.takeIf(String::isNotBlank) ?: "—"
        val numericValue = glucoseValue.replace(',', '.').toFloatOrNull()
        val settings = dailyModel.glucoseLevelSettings
        val glucoseEvents = dailyModel.glucoseEvents
        val trend = glucoseEvents.sortedBy { it.additionTime }.takeLast(2).let { lastEvents ->
            if (lastEvents.size < 2) null else {
                val difference = lastEvents[1].glucoseValue(dailyModel.glucoseFormat) -
                    lastEvents[0].glucoseValue(dailyModel.glucoseFormat)
                GlucoseTrend(
                    direction = when {
                        difference > 0.0 -> GlucoseTrendDirection.UP
                        difference < 0.0 -> GlucoseTrendDirection.DOWN
                        else -> GlucoseTrendDirection.STABLE
                    },
                    valueText = String.format(Locale.US, "%.1f", abs(difference)).replace('.', ',')
                )
            }
        }

        return GlucoseDashboardUiState(
            device = device,
            glucoseValue = glucoseValue,
            deltaText = model.glucoseLevelDifference.format(numberFormat)?.takeIf(String::isNotBlank) ?: "—",
            glucoseTrend = trend,
            tirPercentage = formatTimeInRange(
                glucoseEvents.map { it.glucoseValue(dailyModel.glucoseFormat) },
                settings.normal
            ),
            syncTimeText = "",
            breadUnitsText = model.breadUnitsTotal.format(numberFormat)?.let { "$it ХЕ" } ?: "0,0 ХЕ",
            insulinText = model.insulinTotal.format(numberFormat)?.let { "$it Ед." } ?: "0,0 Ед.",
            glucoseState = when {
                numericValue == null -> GlucoseState.NORMAL
                numericValue.toDouble() in settings.low -> GlucoseState.LOW
                numericValue.toDouble() in settings.high -> GlucoseState.HIGH
                else -> GlucoseState.NORMAL
            },
            chartPoints = detailedPoints.map { GlucosePoint(it.timeLabel, it.value) },
            detailedChartData = DetailedChartData(
                glucosePoints = detailedPoints,
                insulinEntries = DetailedChartItemsBuilder.buildInsulinEntries(detailedPoints, events),
                foodEntries = DetailedChartItemsBuilder.buildFoodEntries(detailedPoints, events),
                activityEntries = DetailedChartItemsBuilder.buildActivityEntries(events),
                dailyGlucoseModel = dailyModel,
                events = events
            )
        )
    }

    private fun Double?.format(format: DecimalFormat): String? =
        this?.let { format.format(it) }
}

internal fun formatTimeInRange(values: List<Double>, normalRange: DoubleRange): String =
    if (values.isEmpty()) "—" else "${values.count { it in normalRange } * 100 / values.size}%"
