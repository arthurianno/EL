package com.elta.android.presentation.features.profile.settings.global.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListAdapter
import com.elta.android.presentation.databinding.ItemProfileSettingsBinding
import com.elta.android.presentation.databinding.ItemProfileSettingsHeaderBinding
import com.elta.android.presentation.databinding.ItemProfileSettingsHealthAppBinding
import com.elta.android.presentation.databinding.ItemProfileSettingsRadioButtonBinding
import com.elta.android.presentation.databinding.ItemSeparatorBinding
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.holder.ProfileSettingRadioButtonViewHolder
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.holder.ProfileSettingsHeaderViewHolder
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.holder.ProfileSettingsHealthAppViewHolder
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.holder.ProfileSettingsSeparatorViewHolder
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.holder.ProfileSettingsViewHolder
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingRadioButtonItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHeaderItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHealthAppItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsItem
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsSeparatorItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class ProfileSettingsAdapter @Inject constructor(
    private val bus: RxBus
) : BaseListAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ProfileSettingsHeaderItem::class.java.hashCode() -> ProfileSettingsHeaderViewHolder(
                ItemProfileSettingsHeaderBinding.inflate(inflater, parent, false)
            )
            ProfileSettingsHealthAppItem::class.java.hashCode() -> ProfileSettingsHealthAppViewHolder(
                ItemProfileSettingsHealthAppBinding.inflate(inflater, parent, false),
                bus
            )
            ProfileSettingsSeparatorItem::class.java.hashCode() -> ProfileSettingsSeparatorViewHolder(
                ItemSeparatorBinding.inflate(inflater, parent, false)
            )
            ProfileSettingsItem::class.java.hashCode() -> ProfileSettingsViewHolder(
                ItemProfileSettingsBinding.inflate(inflater, parent, false),
                bus
            )
            ProfileSettingRadioButtonItem::class.java.hashCode() -> ProfileSettingRadioButtonViewHolder(
                ItemProfileSettingsRadioButtonBinding.inflate(inflater, parent, false),
                bus
            )
            else -> throw IllegalArgumentException("No delegate defined for ${this::class.simpleName}")
        }
    }
}
