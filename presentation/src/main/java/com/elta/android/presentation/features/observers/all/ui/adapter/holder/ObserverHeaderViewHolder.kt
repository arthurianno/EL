package com.elta.android.presentation.features.observers.all.ui.adapter.holder

import androidx.core.view.isVisible
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemProfileSettingsBinding
import com.elta.android.presentation.features.observers.all.ui.adapter.items.ObserverHeaderItem

class ObserverHeaderViewHolder(
    private val binding: ItemProfileSettingsBinding
) : BaseListItemViewHolder<ObserverHeaderItem>(binding.root) {
    override fun bind(item: ObserverHeaderItem) {
        binding.settingsTitleView.text = item.title
        binding.nextIconView.isVisible = false
    }
}
