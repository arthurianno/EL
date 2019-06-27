package com.elta.android.presentation.features.profile.support.ui.adapter.delegates

import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.support.ui.adapter.items.SupportHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder

class SupportHeaderDelegate : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_support_header
    override val itemType: Any = SupportHeaderItem::class

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as SupportHeaderItem
        with(holder as ViewHolder) {
            (itemView as AppCompatTextView).text = item.text
        }
    }
}