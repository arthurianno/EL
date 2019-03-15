package com.elta.android.presentation.widgets.date_picker.adapter.delegates

import android.support.v7.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.widgets.date_picker.adapter.items.DatePickerItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder

class DatePickerDelegate : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_date_picker
    override val itemType: Any = DatePickerItem::class

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as DatePickerItem

        with(holder as ViewHolder) {
        }
    }
}