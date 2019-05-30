package com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHealthAppItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import kotlinx.android.synthetic.main.item_profile_settings_health_app.*

class ProfileSettingsHealthAppDelegate(private val bus: RxBus) : AdapterDelegate() {

    override val itemType = ProfileSettingsHealthAppItem::class
    override val layoutResource = R.layout.item_profile_settings_health_app

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                itemView.setOnClickListener {
                    withAdapterPosition<ProfileSettingsHealthAppItem> { _, item, _ ->
                        bus.click(Clicks.ProfileSettingsHealthAppItemClicked(item.type))
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as ProfileSettingsHealthAppItem

        with(holder as ViewHolder) {
            healthAppIconView.setImageResource(item.icon)
            healthAppTitleView.text = item.title
            healthAppSwitchView.isChecked = item.isActive
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payload: Any) {
        val item = items[position] as ProfileSettingsHealthAppItem
        with(holder as ViewHolder) {
            healthAppSwitchView.isChecked = item.isActive
        }
    }
}