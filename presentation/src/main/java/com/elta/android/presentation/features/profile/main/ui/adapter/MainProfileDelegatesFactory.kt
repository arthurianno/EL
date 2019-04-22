package com.elta.android.presentation.features.profile.main.ui.adapter

import com.elta.android.presentation.features.profile.main.ui.adapter.delegates.MainProfileAdditionalDelegate
import com.elta.android.presentation.features.profile.main.ui.adapter.delegates.MainProfileHeaderDelegate
import com.elta.android.presentation.features.profile.main.ui.adapter.delegates.MainProfileIndicatorDelegate
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileAdditionalItem
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileHeaderItem
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileIndicatorItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class MainProfileDelegatesFactory @Inject constructor(
    private val bus: RxBus
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            MainProfileIndicatorItem::class.java -> MainProfileIndicatorDelegate(bus)
            MainProfileHeaderItem::class.java -> MainProfileHeaderDelegate()
            MainProfileAdditionalItem::class.java -> MainProfileAdditionalDelegate(bus)
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}