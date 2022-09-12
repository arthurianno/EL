package com.elta.android.presentation.features.devices.info.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class DevicePrimaryInfoItem(
    val title: String,
    val isPrimary: Boolean
) : ListItem
