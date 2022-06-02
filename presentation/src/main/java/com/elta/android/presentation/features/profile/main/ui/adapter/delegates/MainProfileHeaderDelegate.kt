package com.elta.android.presentation.features.profile.main.ui.adapter.delegates

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate

class MainProfileHeaderDelegate : AdapterDelegate() {

    override val itemType = MainProfileHeaderItem::class
    override val layoutResource = R.layout.item_profile_header

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as MainProfileHeaderItem
        (holder.itemView as TextView).text = item.title
    }
}
