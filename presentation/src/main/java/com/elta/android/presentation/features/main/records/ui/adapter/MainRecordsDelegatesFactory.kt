package com.elta.android.presentation.features.main.records.ui.adapter

import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import javax.inject.Inject

class MainRecordsDelegatesFactory @Inject constructor() : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}