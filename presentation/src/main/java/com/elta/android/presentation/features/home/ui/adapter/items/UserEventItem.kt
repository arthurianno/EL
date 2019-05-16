package com.elta.android.presentation.features.home.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class UserEventItem(
    val iconRes: Int,
    val titleRes: Int,
    val meta: Any
) : ListItem {

    override fun getUniqueProperty(): Any = titleRes
}