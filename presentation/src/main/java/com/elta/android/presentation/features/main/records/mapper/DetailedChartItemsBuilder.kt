package com.elta.android.presentation.features.main.records.mapper

import com.elta.android.common.utils.CommonFormats
import com.elta.android.common.utils.toStringWithFormat
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.glucoseValue
import com.elta.android.domain.features.diary.home.model.DailyGlucoseModel
import com.elta.android.presentation.features.main.records.ui.compose.DetailedFoodEntry
import com.elta.android.presentation.features.main.records.ui.compose.DetailedGlucosePoint
import com.elta.android.presentation.features.main.records.ui.compose.DetailedInsulinEntry
import org.threeten.bp.Duration
import java.util.Locale

object DetailedChartItemsBuilder {

    private const val MAX_EVENT_CHART_VALUE = 150.0
    private const val MAX_ACTIVITY_DURATION_MINUTES = 12L * 60L
    private const val ACTIVITY_RELEVANCE_WINDOW_MINUTES = 6L * 60L

    fun buildPoints(
        dailyGlucoseModel: DailyGlucoseModel,
        allDayEvents: List<EventV2> = emptyList()
    ): List<DetailedGlucosePoint> {
        val glucoseEvents = dailyGlucoseModel.glucoseEvents.sortedBy { it.additionTime }
        if (glucoseEvents.isEmpty()) return emptyList()

        return glucoseEvents.mapIndexed { index, event ->
            val timeStr = event.additionTime.toStringWithFormat(CommonFormats.FORMAT_TIME)
            val glucoseVal = event.glucoseValue(dailyGlucoseModel.glucoseFormat).toFloat()

            // Calculate trend relative to previous measurement
            val prevVal = if (index > 0) {
                glucoseEvents[index - 1]
                    .takeIf { it.additionTime.toLocalDate() == event.additionTime.toLocalDate() }
                    ?.glucoseValue(dailyGlucoseModel.glucoseFormat)
                    ?.toFloat()
            } else null
            val diff = if (prevVal != null) glucoseVal - prevVal else 0f
            val trendText = when {
                diff > 3.0f -> "быстро растёт"
                diff > 1.0f -> "растёт"
                diff < -3.0f -> "быстро снижается"
                diff < -1.0f -> "снижается"
                else -> "стабилен"
            }
            val trendValStr = if (diff > 0) {
                "+${String.format(Locale.US, "%.1f", diff)}"
            } else {
                String.format(Locale.US, "%.1f", diff)
            }

            // Nearest Food event before/around this measurement
            val foodEvent = allDayEvents
                .filter { it.type is EventType.Bread && it.breadUnitsValue() != null && !it.additionTime.isAfter(event.additionTime) }
                .maxByOrNull { it.additionTime }

            // Nearest Insulin event before/around this measurement
            val insulinEvent = allDayEvents
                .filter { it.type is EventType.Insulin && it.value != null && !it.additionTime.isAfter(event.additionTime) }
                .maxByOrNull { it.additionTime }

            // An old or malformed activity must not be shown as contextual data for
            // a glucose measurement. Use only an activity from the same day that
            // finished no more than six hours before the measurement.
            val activityEvent = allDayEvents
                .asSequence()
                .filter { it.type is EventType.Activity && it.duration != null }
                .filter { it.additionTime.toLocalDate() == event.additionTime.toLocalDate() }
                .filter { activity ->
                    val duration = activity.duration?.toLong() ?: return@filter false
                    duration in 1..MAX_ACTIVITY_DURATION_MINUTES && !activity.additionTime.isAfter(event.additionTime)
                }
                .filter { activity ->
                    val end = activity.additionTime.plusMinutes(activity.duration?.toLong() ?: 0L)
                    Duration.between(end, event.additionTime).toMinutes() <= ACTIVITY_RELEVANCE_WINDOW_MINUTES
                }
                .maxByOrNull { it.additionTime }

            val foodTimeAgoStr = foodEvent?.let {
                val mins = Math.abs(Duration.between(it.additionTime, event.additionTime).toMinutes())
                formatTimeAgo(mins)
            }
            val insulinTimeAgoStr = insulinEvent?.let {
                val mins = Math.abs(Duration.between(it.additionTime, event.additionTime).toMinutes())
                formatTimeAgo(mins)
            }
            val activityTimeAgoStr = activityEvent?.let {
                val mins = Math.abs(Duration.between(it.additionTime, event.additionTime).toMinutes())
                formatTimeAgo(mins)
            }

            DetailedGlucosePoint(
                timeLabel = timeStr,
                value = glucoseVal,
                date = event.additionTime.toLocalDate(),
                isMin = event == dailyGlucoseModel.minEvent,
                isMax = event == dailyGlucoseModel.maxEvent,
                trendText = trendText,
                trendValue = trendValStr,
                foodTimeAgo = foodTimeAgoStr,
                foodUnits = foodEvent?.breadUnitsValue()?.let { "${String.format(Locale.US, "%.1f", it)} ХЕ" },
                insulinTimeAgo = insulinTimeAgoStr,
                insulinUnits = insulinEvent?.value?.let { "${String.format(Locale.US, "%.1f", it)} Ед." },
                activityTimeAgo = activityTimeAgoStr,
                activityDuration = activityEvent?.duration?.let { "$it мин." }
            )
        }
    }

    fun buildInsulinEntries(
        glucosePoints: List<DetailedGlucosePoint>,
        allDayEvents: List<EventV2>
    ): List<DetailedInsulinEntry> {
        if (glucosePoints.isEmpty()) return emptyList()
        val insulinEvents = allDayEvents.filter { it.type is EventType.Insulin && it.value != null }

        return insulinEvents.map { ins ->
            val timeStr = ins.additionTime.toStringWithFormat(CommonFormats.FORMAT_TIME)
            val closestIdx = (0 until glucosePoints.size).minByOrNull { idx ->
                val ptTime = glucosePoints[idx].timeLabel
                Math.abs(timeStr.toMinutes() - ptTime.toMinutes())
            } ?: 0

            val valStr = String.format(Locale.US, "%.1f Ед.", ins.value ?: 0.0)
            val hRatio = ((ins.value ?: 0.0) / MAX_EVENT_CHART_VALUE)
                .coerceIn(0.0, 1.0)
                .toFloat()

            DetailedInsulinEntry(
                timeLabel = timeStr,
                xIndex = closestIdx,
                units = valStr,
                heightRatio = hRatio,
                date = ins.additionTime.toLocalDate(),
                value = ins.value?.toFloat()
            )
        }
    }

    fun buildFoodEntries(
        glucosePoints: List<DetailedGlucosePoint>,
        allDayEvents: List<EventV2>
    ): List<DetailedFoodEntry> {
        if (glucosePoints.isEmpty()) return emptyList()
        val foodEvents = allDayEvents.filter { it.type is EventType.Bread && it.breadUnitsValue() != null }

        return foodEvents.map { food ->
            val timeStr = food.additionTime.toStringWithFormat(CommonFormats.FORMAT_TIME)
            val closestIdx = (0 until glucosePoints.size).minByOrNull { idx ->
                val ptTime = glucosePoints[idx].timeLabel
                Math.abs(timeStr.toMinutes() - ptTime.toMinutes())
            } ?: 0

            val breadUnits = food.breadUnitsValue() ?: 0.0
            val valStr = String.format(Locale.US, "%.1f ХЕ", breadUnits)
            val hRatio = (breadUnits / MAX_EVENT_CHART_VALUE)
                .coerceIn(0.0, 1.0)
                .toFloat()

            DetailedFoodEntry(
                timeLabel = timeStr,
                xIndex = closestIdx,
                breadUnits = valStr,
                heightRatio = hRatio,
                date = food.additionTime.toLocalDate(),
                value = breadUnits.toFloat()
            )
        }
    }

    fun buildActivityEntries(
        allDayEvents: List<EventV2>
    ): List<com.elta.android.presentation.features.main.records.ui.compose.DetailedActivityEntry> {
        val activityEvents = allDayEvents.filter {
            it.type is EventType.Activity && (it.duration?.toLong() ?: 0L) in 1..MAX_ACTIVITY_DURATION_MINUTES
        }
        return activityEvents.map { act ->
            val startStr = act.additionTime.toStringWithFormat(CommonFormats.FORMAT_TIME)
            val endStr = act.additionTime.plusMinutes(act.duration?.toLong() ?: 0L).toStringWithFormat(CommonFormats.FORMAT_TIME)
            com.elta.android.presentation.features.main.records.ui.compose.DetailedActivityEntry(
                startTimeLabel = startStr,
                endTimeLabel = endStr,
                durationMins = act.duration?.toLong() ?: 0L,
                startDate = act.additionTime.toLocalDate(),
                endDate = act.additionTime.plusMinutes(act.duration?.toLong() ?: 0L).toLocalDate()
            )
        }
    }

    private fun formatTimeAgo(minutes: Long): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours > 0 && mins > 0 -> "${hours}ч ${mins}м назад"
            hours > 0 -> "${hours}ч назад"
            else -> "${mins}м назад"
        }
    }

    private fun String.toMinutes(): Int {
        val parts = this.split(":")
        if (parts.size != 2) return 0
        return (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
    }

    private fun EventV2.breadUnitsValue(): Double? = value ?: dishes
        .takeIf { it.isNotEmpty() }
        ?.sumOf { it.breadUnits ?: 0.0 }
}
