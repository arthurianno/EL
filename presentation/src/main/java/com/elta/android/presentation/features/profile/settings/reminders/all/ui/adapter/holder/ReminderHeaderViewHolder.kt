package com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.holder

import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemProfileSettingsHeaderBinding
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.items.ReminderHeaderItem

class ReminderHeaderViewHolder(
    private val binding: ItemProfileSettingsHeaderBinding
) : BaseListItemViewHolder<ReminderHeaderItem>(binding.root) {
    override fun bind(item: ReminderHeaderItem) {
        binding.root.text = item.title
    }
}
