package com.elta.android.presentation.features.profile.settings.global.ui.adapter.holder

import androidx.core.view.isInvisible
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemProfileSettingsBinding
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsItem
import com.nullgr.core.rx.RxBus

class ProfileSettingsViewHolder(
    private val binding: ItemProfileSettingsBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<ProfileSettingsItem>(binding.root) {
    override fun bind(item: ProfileSettingsItem) {
        with(binding) {
            settingsIconView.setImageResource(item.icon)
            settingsTitleView.text = item.title
            when (item.type) {
                ProfileSettingsItem.Type.BIRTH_DATE -> {
                    toggleFocus(true)
                }

                ProfileSettingsItem.Type.BIRTH_DATE_PLACEHOLDER -> {
                    toggleFocus(false)
                }

                ProfileSettingsItem.Type.EMAIL -> {
                    toggleFocus(false)
                }

                ProfileSettingsItem.Type.TOKEN -> {
                    toggleFocus(false)
                    dividerView.isInvisible = true
                }

                ProfileSettingsItem.Type.DELETE_PROFILE -> nextIconView.isInvisible = true
                ProfileSettingsItem.Type.APP_VERSION -> {
                    toggleFocus(false)
                    dividerView.isInvisible = true
                }

                else -> toggleFocus(true)
            }

            itemView.setOnClickListener {
                bus.click(Clicks.ProfileSettingsItemClicked(item.type))
            }
        }
    }

    private fun toggleFocus(isFocus: Boolean) {
        with(binding) {
            settingsTitleView.setTextColor(
                root.context.resources.getColor(
                    if (isFocus) {
                        R.color.black_blue
                    } else {
                        R.color.shade_black2
                    },
                    root.context.theme
                )
            )
            nextIconView.isInvisible = !isFocus
            itemView.isClickable = isFocus
        }
    }
}
