package com.elta.android.presentation.features.statistic.period.ui.adapter.items

import com.elta.android.domain.features.statistics.model.GlucoseStatisticModel
import com.elta.android.presentation.widgets.charts.statistics.models.DateModel
import com.elta.android.presentation.widgets.charts.statistics.models.StatisticsChartDataModel
import com.nullgr.core.adapter.items.ListItem

data class GlucoseStatisticChartItem(
    val datesTitle: String,
    val chartModel: StatisticsChartDataModel
) : ListItem {

    override fun getChangePayload(other: ListItem): Any {
        if (other is GlucoseStatisticChartItem) {
            return mutableSetOf<GlucoseStatisticChartItem.Payload>().apply {
                if (chartModel.isNotTheSame(other.chartModel)) add(Payload.CHART_DATA_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    private fun StatisticsChartDataModel.isNotTheSame(other: StatisticsChartDataModel): Boolean =
        this.maxValue != other.maxValue ||
            this.minValue != other.minValue ||
            this.period != other.period ||
            this.statisticsPerDate contentNotEquals other.statisticsPerDate ||
            this.values != other.values

    private infix fun Map<DateModel, GlucoseStatisticModel?>.contentNotEquals(
        other: Map<DateModel, GlucoseStatisticModel?>
    ): Boolean {
        this.entries.forEach { origin ->
            if (!other.entries.any { it.key == origin.key && it.value == origin.value })
                return true
        }
        return false
    }

    enum class Payload {
        CHART_DATA_CHANGED
    }
}
