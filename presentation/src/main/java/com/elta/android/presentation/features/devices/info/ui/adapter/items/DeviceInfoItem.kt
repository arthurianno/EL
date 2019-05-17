package com.elta.android.presentation.features.devices.info.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class DeviceInfoItem(
    val title: String,
    val description: String,
    var isTheLast: Boolean = false
) : ListItem {

    override fun getUniqueProperty(): Any = title

    override fun getChangePayload(other: ListItem): Any {
        if (other is DeviceInfoItem) {
            return mutableSetOf<Payload>().apply {
                if (isTheLast != other.isTheLast) add(Payload.POSITION_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        POSITION_CHANGED
    }
}