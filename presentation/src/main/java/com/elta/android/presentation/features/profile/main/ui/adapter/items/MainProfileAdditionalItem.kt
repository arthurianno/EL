package com.elta.android.presentation.features.profile.main.ui.adapter.items

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.elta.android.domain.features.user.model.AdditionalFunction
import com.nullgr.core.adapter.items.ListItem

data class MainProfileAdditionalItem(
    @StringRes
    val title: Int,
    @StringRes
    val description: Int? = null,
    @DrawableRes
    val icon: Int,
    val type: AdditionalFunction,
    val showGoArrow: Boolean = true
) : ListItem {

    override fun getUniqueProperty(): Any = type
}
