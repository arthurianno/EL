package com.elta.android.presentation.features.devices.info.ui.adapter.items

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.elta.android.presentation.R
import com.nullgr.core.adapter.items.ListItem

data class DeviceActionItem(
    @DrawableRes val startIcon: Int,
    @StringRes val title: Int,
    @DrawableRes val actionIcon: Int = R.drawable.ic_arrow_left,
    val onClick: () -> Unit
) : ListItem
