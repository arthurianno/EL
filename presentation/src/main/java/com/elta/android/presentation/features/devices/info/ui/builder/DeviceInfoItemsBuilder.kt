package com.elta.android.presentation.features.devices.info.ui.builder

import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.features.devices.info.ui.adapter.items.DeviceInfoItem
import com.elta.android.presentation.features.devices.info.ui.adapter.items.DevicePrimaryInfoItem
import com.elta.android.presentation.utils.toSyncDate
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class DeviceInfoItemsBuilder @Inject constructor(
    private val resources: ResourceProvider,
    private val bus: RxBus
) {

    fun buildItems(info: GlucometerInfo, isPrimary: Boolean) = arrayListOf<ListItem>().apply {
        info.syncDate?.let {
            add(
                DeviceInfoItem(
                    title = resources.getString(R.string.profile_device_info_last_sync_title_field),
                    description = it.toSyncDate(resources),
                    onClick = { bus.click(Clicks.OpenBlueToothScreen) }
                )
            )
        }
        info.softwareVersion?.let {
            add(
                DeviceInfoItem(
                    resources.getString(R.string.profile_device_info_firmware_version_title_field),
                    it.toString()
                )
            )
        }
        info.batteryLevel?.let {
            add(
                DeviceInfoItem(
                    resources.getString(R.string.profile_device_info_charge_level_title_field),
                    resources.getString(R.string.profile_device_info_charge_level_field, it)
                )
            )
        }
        add(
            DevicePrimaryInfoItem(
                resources.getString(R.string.profile_device_info_change_primary_device),
                isPrimary
            )
        )
    }
}
