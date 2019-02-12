package com.elta.android.presentation.features.shops.map.ui.adapter.items

import com.elta.android.presentation.core.ui.adapter.CardType
import com.nullgr.core.adapter.items.ListItem

data class SearchResultItem(
    val id: Any,
    val name: String,
    val address: String,
    val cardType: CardType
) : ListItem {

    override fun getUniqueProperty(): Any = id

    override fun getChangePayload(other: ListItem): Any {
        if (other is SearchResultItem && cardType != other.cardType) {
            return Payload.CARD_TYPE_CHANGED
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        CARD_TYPE_CHANGED
    }
}