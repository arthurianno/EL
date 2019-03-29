package com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsSocialItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import kotlinx.android.synthetic.main.item_profile_settings_social.*

class ProfileSettingsSocialDelegate(private val bus: RxBus) : AdapterDelegate() {

    override val itemType = ProfileSettingsSocialDelegate::class
    override val layoutResource = R.layout.item_profile_settings_social

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                itemView.setOnClickListener {
                    withAdapterPosition<ProfileSettingsSocialItem> { _, item, _ ->
                        bus.click(Clicks.ProfileSettingsSocialItemClicked(item.type))
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as ProfileSettingsSocialItem
        with(holder as ViewHolder) {
            socialNetworkIconView.setImageResource(item.networkIcon)
            socialTitleView.text = item.title
            socialActionIconView.setImageResource(item.actionIcon)
        }
    }
}