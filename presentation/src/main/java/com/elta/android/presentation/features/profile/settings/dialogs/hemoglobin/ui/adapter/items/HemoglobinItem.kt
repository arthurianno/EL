package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

class HemoglobinItem(
    val value: String,
    val date: String,
    val id: String
) : ListItem {
    override fun getUniqueProperty() = id
}
