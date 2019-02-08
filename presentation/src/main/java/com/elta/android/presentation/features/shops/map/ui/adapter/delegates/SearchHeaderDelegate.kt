package com.elta.android.presentation.features.shops.map.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.adapter.bindBackground
import com.elta.android.presentation.core.ui.adapter.bindElevation
import com.elta.android.presentation.features.shops.map.ui.adapter.items.SearchHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate

class SearchHeaderDelegate : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_search_header
    override val itemType: Any = SearchHeaderItem::class

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            bindElevation(this)
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        bindBackground(items, position, holder)
    }
}