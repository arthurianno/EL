package com.elta.android.presentation.features.profile.main.ui.adapter.holder

import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemProfileHeaderBinding
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileHeaderItem

class MainProfileHeaderViewHolder(
    private val binding: ItemProfileHeaderBinding
) : BaseListItemViewHolder<MainProfileHeaderItem>(binding.root) {
    override fun bind(item: MainProfileHeaderItem) {
        binding.root.text = item.title
    }
}
