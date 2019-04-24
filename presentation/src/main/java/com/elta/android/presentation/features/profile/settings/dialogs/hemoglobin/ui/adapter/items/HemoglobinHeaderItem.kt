package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class HemoglobinHeaderItem(val title: String) : ListItem {
    override fun getUniqueProperty() = title
}