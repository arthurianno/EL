package com.elta.android.presentation.core.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import com.nullgr.core.adapter.items.ListItem

class Callback(
    private val before: List<ListItem>,
    private val after: List<ListItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = before.size

    override fun getNewListSize(): Int = after.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        before[oldItemPosition].areItemsTheSame(after[newItemPosition])

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        before[oldItemPosition].areContentsTheSame(after[newItemPosition])

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? =
        before[oldItemPosition].getChangePayload(after[newItemPosition])

    companion object {
        fun new(before: List<ListItem>, after: List<ListItem>) = Callback(before, after)
    }
}
