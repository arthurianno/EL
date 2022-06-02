package com.elta.android.presentation.features.profile.support.ui.adapter

import com.elta.android.presentation.features.profile.support.ui.adapter.delegates.SupportActionDelegate
import com.elta.android.presentation.features.profile.support.ui.adapter.delegates.SupportHeaderDelegate
import com.elta.android.presentation.features.profile.support.ui.adapter.items.SupportActionItem
import com.elta.android.presentation.features.profile.support.ui.adapter.items.SupportHeaderItem
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class SupportDelegatesFactory @Inject constructor(private val bus: RxBus) :
    AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            SupportHeaderItem::class.java -> SupportHeaderDelegate()
            SupportActionItem::class.java -> SupportActionDelegate(bus)
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}
