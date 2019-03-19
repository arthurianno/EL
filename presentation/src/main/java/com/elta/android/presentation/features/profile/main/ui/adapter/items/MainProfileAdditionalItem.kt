package com.elta.android.presentation.features.profile.main.ui.adapter.items

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.nullgr.core.adapter.items.ListItem

data class MainProfileAdditionalItem(
    @StringRes
    val title: Int,
    @StringRes
    val description: Int,
    @DrawableRes
    val icon: Int
) : ListItem