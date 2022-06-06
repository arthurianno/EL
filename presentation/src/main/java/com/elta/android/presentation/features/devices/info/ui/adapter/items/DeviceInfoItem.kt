package com.elta.android.presentation.features.devices.info.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class DeviceInfoItem(
    val title: String,
    val description: String
) : ListItem {

    override fun getUniqueProperty(): Any = title

    override fun getChangePayload(other: ListItem): Any {
        if (other is DeviceInfoItem && this.description != other.description) {
            return Payload.DESCRIPTION_CHANGED
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        DESCRIPTION_CHANGED
    }
}
