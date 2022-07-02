package com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates

import android.content.res.Resources
import android.view.View
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemProfileSettingsBinding
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsItem
import com.nullgr.core.rx.RxBus

class ProfileSettingsViewHolder(
    private val bus: RxBus,
    private val resources: Resources,
    private val binding: ItemProfileSettingsBinding
) : BaseListItemViewHolder<ProfileSettingsItem>(binding.root) {
    private fun toggleFocus(isFocus: Boolean) {
        with(binding) {
            settingsTitleView.setTextColor(resources.getColor(if (isFocus) R.color.black_blue else R.color.shade_black2))
            nextIconView.visibility = if (isFocus) View.VISIBLE else View.INVISIBLE
            itemView.isClickable = isFocus
        }
    }

    override fun bind(item: ProfileSettingsItem) {
        with(binding) {
            settingsIconView.setImageResource(item.icon)
            settingsTitleView.text = item.title
            when (item.type) {
                ProfileSettingsItem.Type.EMAIL -> toggleFocus(false)
                ProfileSettingsItem.Type.APP_VERSION -> {
                    toggleFocus(false)
                    dividerView.visibility = View.INVISIBLE
                }
                else -> toggleFocus(true)
            }

            itemView.setOnClickListener {
                bus.click(Clicks.ProfileSettingsItemClicked(item.type))
            }
        }
    }
}
