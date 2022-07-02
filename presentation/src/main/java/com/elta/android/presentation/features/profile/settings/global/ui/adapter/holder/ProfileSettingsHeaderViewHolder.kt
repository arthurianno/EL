package com.elta.android.presentation.features.profile.settings.global.ui.adapter.holder

import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemProfileSettingsHeaderBinding
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHeaderItem

class ProfileSettingsHeaderViewHolder(
    private val binding: ItemProfileSettingsHeaderBinding
) : BaseListItemViewHolder<ProfileSettingsHeaderItem>(binding.root) {
    override fun bind(item: ProfileSettingsHeaderItem) {
        binding.root.text = item.title
    }
}
