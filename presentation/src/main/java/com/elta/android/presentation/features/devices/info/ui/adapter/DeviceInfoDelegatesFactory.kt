package com.elta.android.presentation.features.devices.info.ui.adapter

import com.elta.android.presentation.features.devices.info.ui.adapter.delegates.DeviceInfoDelegate
import com.elta.android.presentation.features.devices.info.ui.adapter.items.DeviceInfoItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import javax.inject.Inject

class DeviceInfoDelegatesFactory @Inject constructor() : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            DeviceInfoItem::class.java -> DeviceInfoDelegate()
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}