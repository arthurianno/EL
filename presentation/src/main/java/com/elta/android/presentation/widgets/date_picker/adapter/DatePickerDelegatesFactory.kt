package com.elta.android.presentation.widgets.date_picker.adapter

import com.elta.android.presentation.widgets.date_picker.adapter.delegates.DatePickerDelegate
import com.elta.android.presentation.widgets.date_picker.adapter.items.DatePickerItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.AdapterDelegatesFactory
import com.nullgr.core.adapter.items.ListItem

class DatePickerDelegatesFactory : AdapterDelegatesFactory {

    override fun createDelegate(clazz: Class<ListItem>): AdapterDelegate =
        when (clazz) {
            DatePickerItem::class.java -> DatePickerDelegate()
            else -> throw IllegalArgumentException("No delegate defined for ${clazz.simpleName}")
        }
}