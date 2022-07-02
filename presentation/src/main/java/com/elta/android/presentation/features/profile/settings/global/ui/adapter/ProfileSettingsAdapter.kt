package com.elta.android.presentation.features.profile.settings.global.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListAdapter
import com.elta.android.presentation.databinding.ItemProfileSettingsBinding
import com.elta.android.presentation.databinding.ItemProfileSettingsHeaderBinding
import com.elta.android.presentation.databinding.ItemProfileSettingsHealthAppBinding
import com.elta.android.presentation.databinding.ItemProfileSettingsSocialBinding
import com.elta.android.presentation.databinding.ItemSeparatorBinding
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates.ProfileSettingsHeaderViewHolder
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates.ProfileSettingsHealthAppViewHolder
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates.ProfileSettingsSeparatorViewHolder
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates.ProfileSettingsSocialViewHolder
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates.ProfileSettingsViewHolder
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHeaderItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHealthAppItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsSeparatorItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsSocialItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class ProfileSettingsAdapter @Inject constructor(
    private val bus: RxBus
) : BaseListAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ProfileSettingsHeaderItem::class.java.hashCode() -> {
                ProfileSettingsHeaderViewHolder(
                    ItemProfileSettingsHeaderBinding.inflate(inflater, parent, false)
                )
            }
            ProfileSettingsHealthAppItem::class.java.hashCode() -> {
                ProfileSettingsHealthAppViewHolder(
                    bus,
                    ItemProfileSettingsHealthAppBinding.inflate(inflater, parent, false)
                )
            }
            ProfileSettingsSeparatorItem::class.java.hashCode() -> {
                ProfileSettingsSeparatorViewHolder(
                    ItemSeparatorBinding.inflate(inflater, parent, false)
                )
            }
            ProfileSettingsItem::class.java.hashCode() -> {
                val binding = ItemProfileSettingsBinding.inflate(inflater, parent, false)
                ProfileSettingsViewHolder(
                    bus,
                    binding.root.context.resources,
                    binding
                )
            }
            ProfileSettingsSocialItem::class.java.hashCode() -> {
                ProfileSettingsSocialViewHolder(
                    bus,
                    ItemProfileSettingsSocialBinding.inflate(inflater, parent, false)
                )
            }
            else -> throw IllegalArgumentException("No delegate defined for ${this::class.simpleName}")
        }
    }
}