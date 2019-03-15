package com.elta.android.presentation.features.profile.main.ui.adapter

import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import javax.inject.Inject

class MainProfileDelegatesFactory @Inject constructor() : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}