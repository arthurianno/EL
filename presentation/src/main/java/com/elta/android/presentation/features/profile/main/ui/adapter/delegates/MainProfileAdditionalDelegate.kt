package com.elta.android.presentation.features.profile.main.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileAdditionalItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate

class MainProfileAdditionalDelegate : AdapterDelegate() {

    override val itemType = MainProfileAdditionalDelegate::class
    override val layoutResource = R.layout.item_profile_functions_header

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as MainProfileAdditionalItem
        with(holder) {
            (itemView as TextView).text = item.title
        }
    }
}