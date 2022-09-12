package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.holder

import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemHemoglobinHeaderBinding
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.items.HemoglobinHeaderItem

class HemoglobinHeaderViewHolder(
    private val binding: ItemHemoglobinHeaderBinding
) : BaseListItemViewHolder<HemoglobinHeaderItem>(binding.root) {
    override fun bind(item: HemoglobinHeaderItem) {
        binding.headerTitleView.text = item.title
    }
}
