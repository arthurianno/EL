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
            add(DevicesHeaderItem(resources.getString(R.string.profile_devices_primary_device)))
            val (primary, other) = divideGlucometers(glucometers)
            add(mapFromGlucometer(primary))

            if (other.isNotEmpty()) {
                add(DevicesHeaderItem(resources.getString(R.string.profile_devices_other_devices)))
                addAll(other.map(::mapFromGlucometer))
            }
        }
    }

    private fun divideGlucometers(glucometers: List<Glucometer>): Pair<Glucometer, List<Glucometer>> {
        var primary: Glucometer? = null
        val other = mutableListOf<Glucometer>()
        glucometers.forEach {
            if (it.isPrimary) {
                primary = it
            } else {
                other.add(it)
            }
        }
        return checkNotNull(primary) to other
    }

    private fun mapFromGlucometer(source: Glucometer): ListItem =
        with(source) {
            ActiveDeviceItem(
                icon = if (isPrimary) R.drawable.ic_devices else R.drawable.ic_devices_other,
                name = name ?: "",
                address = address,
                isPrimary = isPrimary
            )
        }
}
