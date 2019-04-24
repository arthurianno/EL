package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter

import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.delegates.HemoglobinDelegate
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.delegates.HemoglobinHeaderDelegate
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.items.HemoglobinHeaderItem
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.items.HemoglobinItem
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class HemoglobinEventsDelegatesFactory @Inject constructor(val bus: RxBus) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            HemoglobinHeaderItem::class.java -> HemoglobinHeaderDelegate()
            HemoglobinItem::class.java -> HemoglobinDelegate(bus)
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}