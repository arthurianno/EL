package com.elta.android.presentation.features.main.records.ui.adapter.items

import com.elta.android.presentation.widgets.charts.daily.models.ChartDataModel
import com.nullgr.core.adapter.items.ListItem

data class RecordsDailyGlucoseItem(
    val chartDataModel: ChartDataModel,
    val lastEventTimeTitle: String
) : ListItem {

    override fun getChangePayload(other: ListItem): Any {
        if (other is RecordsDailyGlucoseItem) {
            return mutableSetOf<Payload>().apply {
                if (chartDataModel != other.chartDataModel) add(Payload.CHART_DATA_CHANGED)
                if (lastEventTimeTitle != other.lastEventTimeTitle) add(Payload.LAST_EVENT_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        CHART_DATA_CHANGED,
        LAST_EVENT_CHANGED
    }
}