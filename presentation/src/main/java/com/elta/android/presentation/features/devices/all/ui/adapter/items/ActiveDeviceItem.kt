package com.elta.android.presentation.features.devices.all.ui.adapter.items

import androidx.annotation.DrawableRes
import com.nullgr.core.adapter.items.ListItem

data class ActiveDeviceItem(
    @DrawableRes val icon: Int,
    val name: String,
    val address: String,
    val isPrimary: Boolean
) : ListItem
