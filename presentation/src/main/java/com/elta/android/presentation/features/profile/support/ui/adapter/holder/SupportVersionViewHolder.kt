package com.elta.android.presentation.features.profile.support.ui.adapter.holder

import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemSupportVersionBinding
import com.elta.android.presentation.features.profile.support.ui.adapter.items.SupportVersionItem

class SupportVersionViewHolder(
    private val binding: ItemSupportVersionBinding
) : BaseListItemViewHolder<SupportVersionItem>(binding.root) {
    override fun bind(item: SupportVersionItem) {
        with(binding) {
            versionTitle.text = item.title
            versionText.text = item.version
        }
    }
}
