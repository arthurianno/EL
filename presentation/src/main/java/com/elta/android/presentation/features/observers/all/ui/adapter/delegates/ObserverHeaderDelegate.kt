package com.elta.android.presentation.features.observers.all.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.observers.all.ui.adapter.items.ObserverHeaderItem
import com.nullgr.core.adapter.AdapterDelegate
import com.nullgr.core.adapter.items.ListItem

class ObserverHeaderDelegate : AdapterDelegate() {
    override val itemType = ObserverHeaderItem::class
    override val layoutResource = R.layout.item_profile_settings_header

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as ObserverHeaderItem
        (holder.itemView as TextView).text = item.title
    }
}