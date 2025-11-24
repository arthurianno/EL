package com.elta.android.presentation.features.profile.support.ui.adapter.items

import com.elta.android.presentation.features.profile.support.model.SupportAction
import com.nullgr.core.adapter.items.ListItem

data class SupportVersionItem(
    val title: String,
    val version: String,
    val action: SupportAction? = null
) : ListItem {
    override fun getUniqueProperty() = title
}
