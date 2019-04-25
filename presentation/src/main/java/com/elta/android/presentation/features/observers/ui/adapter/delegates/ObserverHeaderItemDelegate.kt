package com.elta.android.presentation.features.observers.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.elta.android.presentation.R
import com.elta.android.presentation.features.observers.ui.adapter.items.ObserverHeaderItem
import com.nullgr.core.adapter.AdapterDelegate

class ObserverHeaderItemDelegate : AdapterDelegate() {
    override val itemType = ObserverHeaderItem::class
    override val layoutResource = R.layout.item_observer_header

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
        }
    }
}