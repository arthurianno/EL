package com.elta.android.presentation.features.profile.support.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class SupportVersionItem(
    val title: String,
    val version: String
) : ListItem {
    override fun getUniqueProperty() = title
}
