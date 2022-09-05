package com.elta.android.presentation.features.observers.all.ui.adapter.holder

import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemObserverHeaderBinding
import com.elta.android.presentation.features.observers.all.ui.adapter.items.ObserverHeaderItem

class ObserverHeaderViewHolder(
    private val binding: ItemObserverHeaderBinding
) : BaseListItemViewHolder<ObserverHeaderItem>(binding.root) {
    override fun bind(item: ObserverHeaderItem) {
        binding.settingsTitleView.text = item.title
    }
}
