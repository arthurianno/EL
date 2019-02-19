package com.elta.android.presentation.features.main.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class RecordItem(
    val id: Any,
    val icon: Int,
    val title: String,
    val type: String,
    val count: String? = null,
    val date: String
) : ListItem {

    override fun getUniqueProperty(): Any = id
}