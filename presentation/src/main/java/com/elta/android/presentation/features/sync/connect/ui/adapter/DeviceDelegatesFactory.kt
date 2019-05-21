package com.elta.android.presentation.features.sync.connect.ui.adapter

import com.elta.android.presentation.features.sync.connect.ui.adapter.delegates.DeviceDelegate
import com.elta.android.presentation.features.sync.connect.ui.adapter.items.DeviceItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class DeviceDelegatesFactory @Inject constructor(
    private val bus: RxBus
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            DeviceItem::class.java -> DeviceDelegate(bus)
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}