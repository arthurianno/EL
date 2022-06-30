package com.elta.android.presentation.core.ui.adapter

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nullgr.core.adapter.items.ListItem

abstract class BaseListAdapter :
    ListAdapter<ListItem, RecyclerView.ViewHolder>(DefaultDiffCallback()) {
    override fun getItemViewType(position: Int): Int {
        return getItem(position)::class.java.hashCode()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        @Suppress("UNCHECKED_CAST")
        (holder as? BaseListItemViewHolder<ListItem>)?.bind(getItem(position))
    }
}
