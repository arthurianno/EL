package com.elta.android.presentation.features.devices.info.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class DevicePrimaryInfoItem(
    val title: String,
    val isPrimary: Boolean
) : ListItem {

    override fun getChangePayload(other: ListItem): Any {
        if (other is DevicePrimaryInfoItem) {
            return mutableSetOf<Payload>().apply {
                if (isPrimary != other.isPrimary) add(Payload.IS_PRIMARY_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    override fun getUniqueProperty(): Any = title

    enum class Payload {
        IS_PRIMARY_CHANGED
    }
}
