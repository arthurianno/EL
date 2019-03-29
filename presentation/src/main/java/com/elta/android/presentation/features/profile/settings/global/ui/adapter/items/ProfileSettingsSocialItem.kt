package com.elta.android.presentation.features.profile.settings.global.ui.adapter.items

import android.support.annotation.DrawableRes
import com.elta.android.domain.features.auth.model.SocialNetwork
import com.nullgr.core.adapter.items.ListItem

data class ProfileSettingsSocialItem(
    @DrawableRes
    val networkIcon: Int,
    val title: String,
    @DrawableRes
    val actionIcon: Int,
    val type: SocialNetwork
) : ListItem {

    override fun getUniqueProperty() = title
}