package com.elta.android.presentation.features.profile.support.ui.adapter

import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class SupportDelegatesFactory @Inject constructor(private val bus: RxBus) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}