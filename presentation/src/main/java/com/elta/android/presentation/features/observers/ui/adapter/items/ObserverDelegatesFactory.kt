package com.elta.android.presentation.features.observers.ui.adapter.items

import com.elta.android.presentation.features.observers.ui.adapter.delegates.ObserverContentItemDelegate
import com.elta.android.presentation.features.observers.ui.adapter.delegates.ObserverHeaderItemDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class ObserverDelegatesFactory @Inject constructor(
    private val bus: RxBus
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>) = when (clazz) {
        ObserverContentItem::class.java -> ObserverContentItemDelegate(bus)
        ObserverHeaderItem::class.java -> ObserverHeaderItemDelegate()
        else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
    }
}