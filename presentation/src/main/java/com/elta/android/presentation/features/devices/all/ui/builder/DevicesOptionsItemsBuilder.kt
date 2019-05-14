package com.elta.android.presentation.features.devices.all.ui.builder

import com.elta.android.presentation.R
import com.elta.android.presentation.features.devices.all.ui.adapter.items.ActiveDeviceItem
import com.elta.android.presentation.features.devices.all.ui.adapter.items.DevicesHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class DevicesOptionsItemsBuilder @Inject constructor(
    private val resources: ResourceProvider
) {
    fun buildItems() = mutableListOf<ListItem>().apply {
        add(DevicesHeaderItem(resources.getString(R.string.profile_devices_active_glucometers)))
        add(ActiveDeviceItem(
            icon = R.drawable.ic_devices,
            name = "СателлитOnline",
            address = "45:89:21:44"
        ))
    }
}