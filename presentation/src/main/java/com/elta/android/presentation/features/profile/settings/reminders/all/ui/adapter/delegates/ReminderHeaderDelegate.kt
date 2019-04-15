package com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.items.ReminderHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate

class ReminderHeaderDelegate : AdapterDelegate() {

    override val itemType = ReminderHeaderItem::class
    override val layoutResource = R.layout.item_profile_settings_header

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as ReminderHeaderItem
        (holder.itemView as TextView).text = item.title
    }
}