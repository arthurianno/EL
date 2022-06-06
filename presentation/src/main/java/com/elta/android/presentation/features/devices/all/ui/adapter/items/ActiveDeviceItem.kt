package com.elta.android.presentation.features.devices.all.ui.adapter.items

import androidx.annotation.DrawableRes
import com.nullgr.core.adapter.items.ListItem

data class ActiveDeviceItem(
    @DrawableRes val icon: Int,
    val name: String,
    val address: String,
    val isPrimary: Boolean
) : ListItem {

    override fun getChangePayload(other: ListItem): Any {
        if (other is ActiveDeviceItem) {
            return mutableSetOf<Payload>().apply {
                if (name != other.name) add(Payload.NAME_CHANGED)
                if (address != other.address) add(Payload.ADDRESS_CHANGED)
                if (isPrimary != other.isPrimary) add(Payload.IS_PRIMARY_CHANGED)
            }
        }
        return super.getChangePayload(other)
    }

    override fun getUniqueProperty() = address

    enum class Payload {
        NAME_CHANGED,
        ADDRESS_CHANGED,
        IS_PRIMARY_CHANGED
    }
}
