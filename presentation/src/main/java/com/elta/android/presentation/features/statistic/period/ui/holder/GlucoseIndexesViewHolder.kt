package com.elta.android.presentation.features.statistic.period.ui.holder

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemStatGlucoseIndexesSliderBinding
import com.elta.android.presentation.features.statistic.period.ui.adapter.GlucoseItemGroupAdapter
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexesItem
import com.elta.android.presentation.widgets.FixedLinearLayoutManager

class GlucoseIndexesViewHolder(
    binding: ItemStatGlucoseIndexesSliderBinding,
    viewPool: RecyclerView.RecycledViewPool,
    private val adapter: GlucoseItemGroupAdapter
) : BaseListItemViewHolder<GlucoseIndexesItem>(binding.root) {
    init {
        binding.run {
            itemsView.layoutManager =
                FixedLinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL)
            itemsView.setRecycledViewPool(viewPool)
            itemsView.adapter = adapter
        }
    }

    override fun bind(item: GlucoseIndexesItem) {
        adapter.submitList(item.items)
    }
}
