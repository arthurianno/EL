package com.elta.android.presentation.features.devices.all.ui.adapter

import com.elta.android.presentation.features.devices.all.ui.adapter.delegates.ActiveDeviceDelegate
import com.elta.android.presentation.features.devices.all.ui.adapter.delegates.DevicesHeaderDelegate
import com.elta.android.presentation.features.devices.all.ui.adapter.items.ActiveDeviceItem
import com.elta.android.presentation.features.devices.all.ui.adapter.items.DevicesHeaderItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class DevicesDelegatesFactory @Inject constructor(
    private val bus: RxBus
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            DevicesHeaderItem::class.java -> DevicesHeaderDelegate()
            ActiveDeviceItem::class.java -> ActiveDeviceDelegate(bus)
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}
