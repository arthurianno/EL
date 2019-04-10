package com.elta.android.presentation.features.profile.settings.global.ui.adapter.items

import android.support.annotation.DrawableRes
import com.nullgr.core.adapter.items.ListItem

data class ProfileSettingsItem(
    @DrawableRes
    val icon: Int,
    val title: String,
    val type: Type
) : ListItem {

    override fun getUniqueProperty() = title

    enum class Type {
        NAME,
        GENDER,
        PASSWORD,
        EMAIL,
        NOTIFICATION,
        LEGAL_INFO,
        APP_VERSION
    }
}