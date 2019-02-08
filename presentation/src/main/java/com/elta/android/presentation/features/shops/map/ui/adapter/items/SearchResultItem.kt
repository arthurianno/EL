package com.elta.android.presentation.features.shops.map.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class SearchResultItem(
    val id: Any,
    val name: String,
    val address: String
) : ListItem {

    override fun getUniqueProperty(): Any = id
}