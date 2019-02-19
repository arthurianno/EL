package com.elta.android.presentation.features.home.ui.adapter

import com.elta.android.presentation.features.home.ui.adapter.delegates.UserEventDelegate
import com.elta.android.presentation.features.home.ui.adapter.items.UserEventItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class HomeDelegatesFactory @Inject constructor(
    private val bus: RxBus
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate {
        return when (clazz) {
            UserEventItem::class.java -> UserEventDelegate(bus)
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
    }
}