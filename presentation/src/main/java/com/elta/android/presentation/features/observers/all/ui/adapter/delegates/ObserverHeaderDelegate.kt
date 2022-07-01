package com.elta.android.presentation.features.observers.all.ui.adapter.delegates

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.R
import com.elta.android.presentation.databinding.ItemProfileSettingsBinding
import com.elta.android.presentation.features.observers.all.ui.adapter.items.ObserverHeaderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate

class ObserverHeaderDelegate :
    AdapterDelegate<ItemProfileSettingsBinding>(ItemProfileSettingsBinding::inflate) {
    override val itemType = ObserverHeaderItem::class
    override val layoutResource = R.layout.item_profile_settings_header

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as ObserverHeaderItem
        (holder.itemView as TextView).text = item.title
    }
}
