package com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.resources.ResourceProvider
import com.nullgr.core.rx.RxBus
import kotlinx.android.synthetic.main.item_profile_settings.*

class ProfileSettingsDelegate(
    private val bus: RxBus,
    private val resources: ResourceProvider
) : AdapterDelegate() {

    override val itemType = ProfileSettingsItem::class
    override val layoutResource = R.layout.item_profile_settings

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                itemView.setOnClickListener {
                    withAdapterPosition<ProfileSettingsItem> { _, item, _ ->
                        bus.click(Clicks.ProfileSettingsItemClicked(item.type))
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as ProfileSettingsItem
        with(holder as ViewHolder) {
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
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder, payload: Any) {
        val item = items[position] as ProfileSettingsItem
        with(holder as ViewHolder) {
            when (payload) {
                ProfileSettingsItem.Payload.TITLE_CHANGED -> settingsTitleView.text = item.title
            }
        }
    }

    private fun ViewHolder.toggleFocus(isFocus: Boolean) {
        settingsTitleView.setTextColor(resources.getColor(if (isFocus) R.color.black_blue else R.color.shade_black2))
        nextIconView.visibility = if (isFocus) View.VISIBLE else View.INVISIBLE
        itemView.isClickable = isFocus
    }
}