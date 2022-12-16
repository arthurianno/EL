package com.elta.android.presentation.features.sync.connect.base.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class DeviceItem(
    val id: Any,
    val name: String,
    val address: String,
    val isSelected: Boolean,
    val isLast: Boolean
) : ListItem
