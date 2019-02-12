package com.elta.android.presentation.features.shops.map.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class ShopItem(
    val id: Any,
    val name: String,
    val address: String,
    val distance: String?,
    val phone: String?
) : ListItem {

    override fun getChangePayload(other: ListItem): Any {
        if (other is ShopItem && distance != other.distance) {
            return Payload.DISTANCE_CHANGED
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        DISTANCE_CHANGED
    }
}