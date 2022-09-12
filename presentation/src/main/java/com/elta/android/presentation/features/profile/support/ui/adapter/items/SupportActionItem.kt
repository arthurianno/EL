package com.elta.android.presentation.features.profile.support.ui.adapter.items

import com.elta.android.presentation.features.profile.support.model.SupportAction
import com.nullgr.core.adapter.items.ListItem

data class SupportActionItem(
    val icon: Int,
    val title: String,
    val subTitle: String,
    val action: SupportAction
) : ListItem {
    override fun getUniqueProperty() = icon
}
