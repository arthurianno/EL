package com.elta.android.presentation.features.profile.settings.global.ui.adapter.items

import android.support.annotation.DrawableRes
import com.nullgr.core.adapter.items.ListItem

data class ProfileSettingsItem(
    @DrawableRes
    val icon: Int,
    val title: String,
    val type: Type
) : ListItem {

    override fun getUniqueProperty() = type

    override fun getChangePayload(other: ListItem): Any {
        if (other is ProfileSettingsItem && title != other.title) {
            return Payload.TITLE_CHANGED
        }
        return super.getChangePayload(other)
    }

    enum class Payload {
        TITLE_CHANGED
    }

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