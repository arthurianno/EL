package com.elta.android.presentation.features.main.records.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elta.android.presentation.core.ui.adapter.DefaultDiffCallback
import com.elta.android.presentation.databinding.ItemRecordBinding
import com.elta.android.presentation.features.main.records.ui.adapter.holder.ItemRecordViewHolder
import com.elta.android.presentation.features.main.records.ui.adapter.items.RecordItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.rx.RxBus

class RecordItemGroupAdapter(
    private val bus: RxBus
) : ListAdapter<ListItem, ItemRecordViewHolder>(DefaultDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemRecordViewHolder {
        val binding = ItemRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemRecordViewHolder(binding, bus)
    }

    override fun onBindViewHolder(holder: ItemRecordViewHolder, position: Int) {
        (getItem(position) as? RecordItem)?.let { holder.bind(it) }
    }
}
