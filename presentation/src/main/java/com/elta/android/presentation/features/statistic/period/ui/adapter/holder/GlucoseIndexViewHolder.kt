package com.elta.android.presentation.features.statistic.period.ui.adapter.holder

import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemStatGlucoseIndexBinding
import com.elta.android.presentation.features.statistic.period.ui.adapter.items.GlucoseIndexItem

class GlucoseIndexViewHolder(
    private val binding: ItemStatGlucoseIndexBinding
) : BaseListItemViewHolder<GlucoseIndexItem>(binding.root) {
    override fun bind(item: GlucoseIndexItem) {
        binding.run {
            itemView.background = item.bg
            indexValueView.text = item.value
            indexUnitView.text = item.unit
            indexDescriptionView.text = item.description
        }
    }
}
