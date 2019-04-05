package com.elta.android.presentation.features.profile.settings.global.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.elta.android.presentation.R
import com.elta.android.presentation.features.profile.settings.global.ui.adapter.items.ProfileSettingsHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate

class ProfileSettingsHeaderDelegate : AdapterDelegate() {

    override val itemType = ProfileSettingsHeaderDelegate::class
    override val layoutResource = R.layout.item_profile_settings_header

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as ProfileSettingsHeaderItem
        (holder.itemView as TextView).text = item.title
    }
}