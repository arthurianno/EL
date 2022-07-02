package com.elta.android.presentation.features.profile.support.ui.adapter.holder

import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemSupportHeaderBinding
import com.elta.android.presentation.features.profile.support.ui.adapter.items.SupportHeaderItem

class SupportHeaderViewHolder(
    private val binding: ItemSupportHeaderBinding
) : BaseListItemViewHolder<SupportHeaderItem>(binding.root) {
    override fun bind(item: SupportHeaderItem) {
        binding.root.text = item.text
    }
}
