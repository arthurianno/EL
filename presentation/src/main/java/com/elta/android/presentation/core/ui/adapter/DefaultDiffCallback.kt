package com.elta.android.presentation.core.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.nullgr.core.adapter.items.ListItem

class DefaultDiffCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
        newItem.areItemsTheSame(oldItem)

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean =
        newItem.areContentsTheSame(oldItem)
}
