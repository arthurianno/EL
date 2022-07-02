package com.elta.android.presentation.features.profile.settings.global.ui.adapter.holder

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemProfileSettingsHealthAppBinding
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHealthAppItem
import com.nullgr.core.rx.RxBus

class ProfileSettingsHealthAppViewHolder(
    private val binding: ItemProfileSettingsHealthAppBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<ProfileSettingsHealthAppItem>(binding.root) {
    override fun bind(item: ProfileSettingsHealthAppItem) {
        with(binding) {
            healthAppIconView.setImageResource(item.icon)
            healthAppTitleView.text = item.title
            healthAppSwitchView.isChecked = item.isActive
            itemView.setOnClickListener {
                bus.click(Clicks.ProfileSettingsHealthAppItemClicked(item.type))
            }
        }
    }
}
