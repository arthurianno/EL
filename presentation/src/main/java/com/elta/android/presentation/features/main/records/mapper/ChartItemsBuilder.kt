package com.elta.android.presentation.features.main.records.mapper

import com.elta.android.domain.features.diary.home.model.DailyGlucoseModel
import com.elta.android.presentation.widgets.charts.daily.models.ChartDataModel
import com.elta.android.presentation.widgets.charts.daily.models.ChartItemModel
import com.elta.android.presentation.widgets.charts.daily.models.ChartRangesModel

object ChartItemsBuilder {

    fun build(glucoseModel: DailyGlucoseModel): ChartDataModel {
        return ChartDataModel(glucoseModel.items(), glucoseModel.ranges())
    }

    private fun DailyGlucoseModel.items(): List<ChartItemModel> {
        return arrayListOf<ChartItemModel>().apply {
            glucoseEvents.forEach {
                add(
                    ChartItemModel(
                        value = it.value ?: 0.0,
                        dateTime = it.additionTime,
                        isMaxValue = it == maxEvent,
                        isMinValue = it == minEvent,
                        isLastValue = it == lastEvent
                    )
                )
            }
        }
    }

    private fun DailyGlucoseModel.ranges(): ChartRangesModel {
        val start = when {
            minEvent != null -> glucoseLevelSettings.low.start
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