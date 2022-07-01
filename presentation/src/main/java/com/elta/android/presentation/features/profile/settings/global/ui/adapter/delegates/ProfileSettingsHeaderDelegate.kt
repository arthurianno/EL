package com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.databinding.ItemProfileSettingsHeaderBinding
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate

class ProfileSettingsHeaderDelegate :
    AdapterDelegate<ItemProfileSettingsHeaderBinding>(ItemProfileSettingsHeaderBinding::inflate) {

    override val itemType = ProfileSettingsHeaderItem::class
    override val layoutResource = R.layout.item_profile_settings_header

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as ProfileSettingsHeaderItem
        (holder.itemView as TextView).text = item.title
    }
}
