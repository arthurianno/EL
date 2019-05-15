package com.elta.android.presentation.features.devices.all.ui.adapter

import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import javax.inject.Inject

class DevicesDelegatesFactory @Inject constructor() : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate {
        throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
    }
}