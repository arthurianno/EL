package com.elta.android.presentation.features.sync.connect.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class DeviceItem(
    val id: Any,
    val name: String,
    val address: String,
    val isSelected: Boolean
) : ListItem {

    override fun getUniqueProperty(): Any = id

    override fun getChangePayload(other: ListItem): Any {
        if (other is DeviceItem) {
            return mutableSetOf<Payload>().apply {
                if (isSelected != other.isSelected) add(Payload.SELECTION_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        SELECTION_CHANGED
    }
}