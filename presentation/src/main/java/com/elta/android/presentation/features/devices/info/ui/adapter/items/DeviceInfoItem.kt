package com.elta.android.presentation.features.devices.info.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class DeviceInfoItem(
    val title: String,
    val description: String
) : ListItem {

    override fun getUniqueProperty(): Any = title
}