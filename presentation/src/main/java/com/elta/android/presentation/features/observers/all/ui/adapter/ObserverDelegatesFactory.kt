package com.elta.android.presentation.features.observers.all.ui.adapter

import com.elta.android.presentation.features.observers.all.ui.adapter.delegates.ObserverDelegate
import com.elta.android.presentation.features.observers.all.ui.adapter.delegates.ObserverHeaderDelegate
import com.elta.android.presentation.features.observers.all.ui.adapter.items.ObserverItem
import com.elta.android.presentation.features.observers.all.ui.adapter.items.ObserverHeaderItem
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class ObserverDelegatesFactory @Inject constructor(
    private val bus: RxBus
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>) = when (clazz) {
        ObserverItem::class.java -> ObserverDelegate(bus)
        ObserverHeaderItem::class.java -> ObserverHeaderDelegate()
        else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
    }
}