package com.elta.android.presentation.widgets.spinner.adapter

import com.elta.android.presentation.widgets.spinner.SelectItemListener
import com.elta.android.presentation.widgets.spinner.adapter.delegates.SpinnerDelegate
import com.elta.android.presentation.widgets.spinner.adapter.items.SpinnerItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.resources.ResourceProvider
import javax.inject.Inject

class SpinnerDelegatesFactory @Inject constructor(
    private val resources: ResourceProvider,
    private val listener: SelectItemListener
) : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            SpinnerItem::class.java -> SpinnerDelegate(resources, listener)
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}
