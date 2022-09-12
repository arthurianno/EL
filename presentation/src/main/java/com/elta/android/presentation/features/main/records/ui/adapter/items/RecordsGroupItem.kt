package com.elta.android.presentation.features.main.records.ui.adapter.items

import com.elta.android.presentation.core.ui.adapter.GroupItem
import com.elta.android.presentation.core.ui.adapter.isChanged
import com.nullgr.core.adapter.items.ListItem

data class RecordsGroupItem(
    val id: Any,
    val icon: Int,
    val name: String,
    override var isExpanded: Boolean = false,
    override val items: List<ListItem>
) : GroupItem {

    override fun getUniqueProperty(): Any = id

    override fun getChangePayload(other: ListItem): Any {
        if (other is RecordsGroupItem) {
            return mutableSetOf<Payload>().apply {
                if (isExpanded != other.isExpanded) add(Payload.EXPANDED_STATE_CHANGED)
                if (items.isChanged(other.items)) add(Payload.ITEMS_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        EXPANDED_STATE_CHANGED,
        ITEMS_CHANGED
    }
}
