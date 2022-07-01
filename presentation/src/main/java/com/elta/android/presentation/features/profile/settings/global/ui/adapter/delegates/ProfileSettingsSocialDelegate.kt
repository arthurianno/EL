package com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.databinding.ItemProfileSettingsSocialBinding
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsSocialItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus

class ProfileSettingsSocialDelegate(private val bus: RxBus) :
    AdapterDelegate<ItemProfileSettingsSocialBinding>(ItemProfileSettingsSocialBinding::inflate) {

    override val itemType = ProfileSettingsSocialItem::class
    override val layoutResource = R.layout.item_profile_settings_social

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                itemView.setOnClickListener {
                    withAdapterPosition<ProfileSettingsSocialItem> { _, item, _ ->
                        bus.click(Clicks.ProfileSettingsSocialItemClicked(item))
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as ProfileSettingsSocialItem

        with(binding) {
            socialNetworkIconView.setImageResource(item.networkIcon)
            socialTitleView.text = item.title
            socialActionIconView.setImageResource(item.getActionIcon())
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payload: Any
    ) {
        val item = items[position] as ProfileSettingsSocialItem
        binding.socialActionIconView.setImageResource(item.getActionIcon())
    }

    private fun ProfileSettingsSocialItem.getActionIcon() =
        if (isLinked) R.drawable.ic_delete else R.drawable.ic_add
}
