package com.elta.android.presentation.features.main.records.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

// TODO provide real classes
data class RecordsDailyGlucoseItem(
    val glucoseChartModels: List<Any>,
    val glucoseLevelSettings: Any,
    val lastEventTimeTitle: String
) : ListItem {

    override fun getChangePayload(other: ListItem): Any {
        if (other is RecordsDailyGlucoseItem) {
            return mutableSetOf<Payload>().apply {
                if (glucoseChartModels != other.glucoseChartModels ||
                    glucoseLevelSettings != other.glucoseLevelSettings) add(Payload.ITEMS_CHANGED)
                if (lastEventTimeTitle != other.lastEventTimeTitle) add(Payload.LAST_EVENT_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        ITEMS_CHANGED,
        LAST_EVENT_CHANGED
    }
}