package com.elta.android.presentation.features.devices.all.ui.builder

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.presentation.R
import com.elta.android.presentation.features.devices.all.ui.adapter.items.ActiveDeviceItem
import com.elta.android.presentation.features.devices.all.ui.adapter.items.DevicesHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class DevicesOptionsItemsBuilder @Inject constructor(
    private val resources: ResourceProvider
) {
    fun buildItems(glucometers: List<Glucometer>) = mutableListOf<ListItem>().apply {
        if (glucometers.isNotEmpty()) {
            add(DevicesHeaderItem(resources.getString(R.string.profile_devices_active_glucometers)))
            addAll(glucometers.map(::mapFromGlucometer))
        }
    }

    private fun mapFromGlucometer(source: Glucometer): ListItem =
        with(source) {
            ActiveDeviceItem(
                icon = R.drawable.ic_devices,
                name = name ?: "",
                address = address
            )
        }
}