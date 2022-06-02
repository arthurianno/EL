package com.elta.android.presentation.features.devices.all.ui.adapter.delegates

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.devices.all.ui.adapter.items.DevicesHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate

class DevicesHeaderDelegate : AdapterDelegate() {

    override val itemType = DevicesHeaderItem::class
    override val layoutResource = R.layout.item_devices_header

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as DevicesHeaderItem
        (holder.itemView as TextView).text = item.title
    }
}
