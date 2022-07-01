package com.elta.android.presentation.features.main.events.chooser.ui.adapter.holder

import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemChooserHeaderBinding
import com.elta.android.presentation.features.main.events.chooser.ui.adapter.items.ChooserHeaderItem

class ChooserHeaderViewHolder(private val binding: ItemChooserHeaderBinding) :
    BaseListItemViewHolder<ChooserHeaderItem>(binding.root) {
    override fun bind(item: ChooserHeaderItem) {
        binding.root.text = item.title
    }
}
