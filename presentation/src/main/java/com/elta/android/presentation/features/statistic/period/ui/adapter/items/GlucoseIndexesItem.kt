package com.elta.android.presentation.features.statistic.period.ui.adapter.items

import com.elta.android.presentation.core.ui.adapter.ParentItem
import com.elta.android.presentation.core.ui.adapter.isChanged
import com.nullgr.core.adapter.items.ListItem

data class GlucoseIndexesItem(
    override val items: List<ListItem>
) : ParentItem {

    override fun getChangePayload(other: ListItem): Any {
        if (other is GlucoseIndexesItem && items.isChanged(other.items)) {
            return Payload.ITEMS_CHANGED
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        ITEMS_CHANGED
    }
}
