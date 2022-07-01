package com.elta.android.presentation.core.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.nullgr.core.adapter.items.ListItem

abstract class BaseListItemViewHolder<in I : ListItem>(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun bind(item: I)
}
