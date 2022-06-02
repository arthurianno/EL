package com.elta.android.presentation.core.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.nullgr.core.adapter.items
import com.nullgr.core.adapter.items.ListItem

@Suppress("ReturnCount")
fun <T : ListItem> List<T>.isChanged(other: List<T>): Boolean {
    if (size != other.size) {
        return true
    }

    forEachIndexed { index, item ->
        if (!item.areItemsTheSame(other[index])) {
            return true
        } else if (!item.areContentsTheSame(other[index])) {
            return true
        }
    }

    return false
}

inline fun <reified T : ListItem> RecyclerView.ViewHolder.withAdapterPosition(
    block: (items: List<ListItem>, item: T, position: Int) -> Unit
) {
    with(adapterPosition) {
        if (this != RecyclerView.NO_POSITION) {
            val items = items()
            if (items != null && this >= 0 && this < items.size) {
                block.invoke(items, items[this] as T, this)
            }
        }
    }
}
