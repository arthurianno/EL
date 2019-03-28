package com.elta.android.presentation.features.profile.settings.global.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class ProfileSettingsHeaderItem(val title: String) : ListItem {

    override fun getUniqueProperty() = title
}