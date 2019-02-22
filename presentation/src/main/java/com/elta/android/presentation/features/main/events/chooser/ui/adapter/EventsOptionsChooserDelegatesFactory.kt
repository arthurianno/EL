package com.elta.android.presentation.features.main.events.chooser.ui.adapter

import com.elta.android.presentation.features.main.events.chooser.ui.adapter.delegate.ChooserHeaderDelegate
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserHeaderItem
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import javax.inject.Inject

class EventsOptionsChooserDelegatesFactory @Inject constructor() : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            ChooserHeaderItem::class.java -> ChooserHeaderDelegate()
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}