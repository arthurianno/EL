package com.elta.android.presentation.features.statistic.period.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.elta.android.presentation.core.ui.adapter.DefaultDiffCallback
import com.elta.android.presentation.databinding.ItemStatGlucoseIndexBinding
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexItem
import com.elta.android.presentation.features.statistic.period.ui.holder.GlucoseIndexViewHolder
import com.nullgr.core.adapter.items.ListItem

class GlucoseItemGroupAdapter :
    ListAdapter<ListItem, GlucoseIndexViewHolder>(DefaultDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GlucoseIndexViewHolder {
        val binding =
            ItemStatGlucoseIndexBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GlucoseIndexViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GlucoseIndexViewHolder, position: Int) {
        (getItem(position) as? GlucoseIndexItem)?.let { holder.bind(it) }
    }
}
