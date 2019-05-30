package com.elta.android.presentation.features.profile.main.ui.adapter.items

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.elta.android.domain.features.user.model.AdditionalFunction
import com.nullgr.core.adapter.items.ListItem

data class MainProfileAdditionalItem(
    @StringRes
    val title: Int,
    @StringRes
    val description: Int? = null,
    @DrawableRes
    val icon: Int,
    val type: AdditionalFunction
) : ListItem {

    override fun getUniqueProperty(): Any = type
}