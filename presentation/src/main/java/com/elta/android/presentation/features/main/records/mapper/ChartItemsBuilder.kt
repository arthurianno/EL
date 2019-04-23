package com.elta.android.presentation.features.main.records.mapper

import com.elta.android.domain.features.diary.home.model.DailyGlucoseModel
import com.elta.android.domain.features.diary.home.model.GlucoseLevelSettings
import com.elta.android.presentation.utils.hourOfDay
import com.elta.android.presentation.utils.minute
import com.elta.android.presentation.widgets.charts.daily.models.ChartDataModel
import com.elta.android.presentation.widgets.charts.daily.models.ChartItemModel
import com.elta.android.presentation.widgets.charts.daily.models.ChartItemValueType
import com.elta.android.presentation.widgets.charts.daily.models.ChartRangesModel
import com.nullgr.core.date.CommonFormats
import com.nullgr.core.date.toStringWithFormat
import java.util.Calendar

object ChartItemsBuilder {

    private val mapDateCalendar = Calendar.getInstance()

    fun build(glucoseModel: DailyGlucoseModel) =
        ChartDataModel(glucoseModel.items(), glucoseModel.ranges())

    private fun DailyGlucoseModel.items() =
        arrayListOf<ChartItemModel>().apply {
            glucoseEvents.forEach {
                mapDateCalendar.time = it.additionTime
                add(
                    ChartItemModel(
                        value = it.value ?: 0.0,
                        dateTime = it.additionTime,
                        formattedTime = it.additionTime.toStringWithFormat(CommonFormats.FORMAT_TIME),
                        hourOfEvent = mapDateCalendar.hourOfDay,
                        minutesOfEvent = mapDateCalendar.minute,
                        valueType = it.value.toValueType(glucoseLevelSettings),
                        isMaxValue = it == maxEvent,
                        isMinValue = it == minEvent,
                        isLastValue = it == lastEvent
                    )
                )
            }
        }

    private fun Double?.toValueType(glucoseLevelSettings: GlucoseLevelSettings) =
        when (this ?: 0.0) {
            in glucoseLevelSettings.low -> ChartItemValueType.LOW
            in glucoseLevelSettings.high -> ChartItemValueType.HIGH
            else -> ChartItemValueType.NORMAL
        }

    private fun DailyGlucoseModel.ranges(): ChartRangesModel {
        val start = when {
            minEvent != null -> minEvent?.value ?: glucoseLevelSettings.low.start
            else -> glucoseLevelSettings.normal.start
        }
        val lowMax = when {
            minEvent != null -> glucoseLevelSettings.low.end
            else -> null
        }
        val normalMax = glucoseLevelSettings.normal.end
        val highMax = maxEvent?.value
        val end = highMax ?: glucoseLevelSettings.normal.end
        return ChartRangesModel(start, end, normalMax, lowMax, highMax)
    }
}