package com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemProfileSettingsSocialBinding
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsSocialItem
import com.nullgr.core.rx.RxBus

class ProfileSettingsSocialViewHolder(
    private val bus: RxBus,
    private val binding: ItemProfileSettingsSocialBinding
) :
    BaseListItemViewHolder<ProfileSettingsSocialItem>(binding.root) {
    private fun ProfileSettingsSocialItem.getActionIcon() =
        if (isLinked) R.drawable.ic_delete else R.drawable.ic_add

    override fun bind(item: ProfileSettingsSocialItem) {
        with(binding) {
            socialNetworkIconView.setImageResource(item.networkIcon)
            socialTitleView.text = item.title
            socialActionIconView.setImageResource(item.getActionIcon())
            itemView.setOnClickListener {
                bus.click(Clicks.ProfileSettingsSocialItemClicked(item))
            }
        }
    }
}
