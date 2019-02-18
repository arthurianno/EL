package com.elta.android.presentation.features.home.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.home.ui.adapter.items.EventItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import kotlinx.android.synthetic.main.item_event.*

class EventDelegate(private val bus: RxBus) : AdapterDelegate() {
    override val itemType = EventItem::class
    override val layoutResource = R.layout.item_event

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        super.onBindViewHolder(items, position, holder)
        val item = items[position] as EventItem

        with(holder as ViewHolder) {
            eventIconView.setImageResource(item.iconRes)
            eventTitleView.setText(item.titleRes)
        }
    }
}