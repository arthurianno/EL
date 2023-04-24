package com.elta.android.presentation.features.devices.all.ui.builder

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.presentation.R
import com.elta.android.presentation.features.devices.all.ui.adapter.items.ActiveDeviceItem
import com.elta.android.presentation.features.devices.all.ui.adapter.items.DevicesHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class DevicesOptionsItemsBuilder @Inject constructor(
    private val resources: ResourceProvider
) {
    fun buildItems(glucometers: List<Pair<Glucometer, GlucometerInfo>>) =
        mutableListOf<ListItem>().apply {
            if (glucometers.isNotEmpty()) {
                add(DevicesHeaderItem(resources.getString(R.string.profile_devices_primary_device)))
                val (primary, other) = glucometers.run {
                    (
                        firstOrNull { it.first.isPrimary }
                            ?: first()
                        ) to filterNot { it.first.isPrimary }
                }
                add(
                    mapFromGlucometer(
                        primary.first,
                        primary.second.glucometerSerialNumber.orEmpty()
                    )
                )

                if (other.isNotEmpty()) {
                    add(DevicesHeaderItem(resources.getString(R.string.profile_devices_other_devices)))
                    addAll(
                        other.map {
                            mapFromGlucometer(it.first, it.second.glucometerSerialNumber.orEmpty())
                        }
                    )
                }
            }
        }

    private fun mapFromGlucometer(source: Glucometer, serial: String): ListItem =
        with(source) {
            ActiveDeviceItem(
                icon = if (isPrimary) R.drawable.ic_primary_device else R.drawable.ic_devices,
                name = name ?: "",
                address = address,
                serial = serial,
                isPrimary = isPrimary
            )
        }
}
