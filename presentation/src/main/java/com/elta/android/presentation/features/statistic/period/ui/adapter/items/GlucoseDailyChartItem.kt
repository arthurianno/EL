package com.elta.android.presentation.features.statistic.period.ui.adapter.items

import com.elta.android.presentation.widgets.charts.daily.models.ChartDataModel
import com.nullgr.core.adapter.items.ListItem

data class GlucoseDailyChartItem(
    val chartDataModel: ChartDataModel,
    val dateTitle: String
) : ListItem {

    override fun getChangePayload(other: ListItem): Any {
        if (other is GlucoseDailyChartItem) {
            return mutableSetOf<Payload>().apply {
                if (chartDataModel != other.chartDataModel) add(Payload.CHART_DATA_CHANGED)
                if (dateTitle != other.dateTitle) add(Payload.LAST_EVENT_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        CHART_DATA_CHANGED,
        LAST_EVENT_CHANGED
    }
}
