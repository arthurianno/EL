package com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.holder

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemProfileAdditionalSettingsBinding
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.items.ReminderItem
import com.nullgr.core.rx.RxBus

class ReminderViewHolder(
    private val binding: ItemProfileAdditionalSettingsBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<ReminderItem>(binding.root) {
    override fun bind(item: ReminderItem) {
        with(binding) {
            settingsTypeIconView.setImageResource(item.type)
            settingsActionIconView.setImageResource(item.action)
            settingsNameView.text = item.title
            settingsDescriptionNameView.text = item.description
            itemView.setOnClickListener {
                bus.click(Clicks.ReminderItemClicked(item))
            }
        }
    }
}
