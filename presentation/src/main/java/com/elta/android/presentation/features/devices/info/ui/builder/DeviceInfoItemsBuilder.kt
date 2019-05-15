package com.elta.android.presentation.features.devices.info.ui.builder

import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.presentation.R
import com.elta.android.presentation.features.devices.info.ui.adapter.items.DeviceInfoItem
import com.elta.android.presentation.utils.toEventTime
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class DeviceInfoItemsBuilder @Inject constructor(
    private val resources: ResourceProvider
) {

    fun buildItems(info: GlucometerInfo) = arrayListOf<ListItem>().apply {
        info.modificationTime?.let {
            add(DeviceInfoItem(
                resources.getString(R.string.profile_device_info_last_sync_title_field),
                it.toEventTime(resources)))
        }
        info.softwareVersion?.let {
            add(DeviceInfoItem(
                resources.getString(R.string.profile_device_info_firmware_version_title_field),
                it.toString()))
        }
        info.batteryLevel?.let {
            add(DeviceInfoItem(
                resources.getString(R.string.profile_device_info_charge_level_title_field),
                resources.getString(R.string.profile_device_info_charge_level_field, it)))
        }
    }
}