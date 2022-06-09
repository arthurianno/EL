package com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.delegates

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.withAdapterPosition
import com.elta.android.presentation.databinding.ItemProfileAdditionalSettingsBinding
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.items.ReminderItem
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.resources.ResourceProvider
import com.nullgr.core.rx.RxBus

class ReminderDelegate(
    private val bus: RxBus,
    private val resources: ResourceProvider
) : AdapterDelegate<ItemProfileAdditionalSettingsBinding>(ItemProfileAdditionalSettingsBinding::inflate) {

    override val itemType = ReminderItem::class
    override val layoutResource = R.layout.item_profile_additional_settings

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                itemView.setOnClickListener {
                    withAdapterPosition<ReminderItem> { _, item, _ ->
                        bus.click(Clicks.ReminderItemClicked(item))
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
        val item = items[position] as ReminderItem
        with(binding) {
            settingsTypeIconView.setImageResource(item.type)
            settingsActionIconView.setImageResource(item.action)
            settingsNameView.text = item.title
            settingsDescriptionNameView.text = item.description
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payload: Any
    ) {
        val item = items[position] as ReminderItem
        with(binding) {
            when (payload) {
                ReminderItem.Payload.TITLE_CHANGED -> settingsNameView.text = item.title
                ReminderItem.Payload.DESCRIPTION_CHANGED ->
                    settingsDescriptionNameView.text =
                        item.description
            }
        }
    }
}
