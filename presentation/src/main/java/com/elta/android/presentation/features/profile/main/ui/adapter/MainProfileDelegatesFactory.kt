package com.elta.android.presentation.features.profile.main.ui.adapter

import com.elta.android.presentation.features.profile.main.ui.adapter.delegates.MainProfileAdditionalDelegate
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileAdditionalItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import javax.inject.Inject

class MainProfileDelegatesFactory @Inject constructor() : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            MainProfileAdditionalItem::class.java -> MainProfileAdditionalDelegate()
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}