package com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.core.ui.adapter.BaseListAdapter
import com.elta.android.presentation.databinding.ItemProfileAdditionalSettingsBinding
import com.elta.android.presentation.databinding.ItemProfileSettingsHeaderBinding
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.holder.ReminderHeaderViewHolder
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.holder.ReminderViewHolder
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.items.ReminderHeaderItem
import com.elta.android.presentation.features.profile.settings.reminders.all.ui.adapter.items.ReminderItem
import com.nullgr.core.rx.RxBus
import javax.inject.Inject

class ReminderAdapter @Inject constructor(
    private val bus: RxBus
) : BaseListAdapter() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ReminderHeaderItem::class.java.hashCode() -> {
                ReminderHeaderViewHolder(
                    ItemProfileSettingsHeaderBinding.inflate(inflater, parent, false)
                )
            }
            ReminderItem::class.java.hashCode() -> {
                ReminderViewHolder(
                    ItemProfileAdditionalSettingsBinding.inflate(inflater, parent, false),
                    bus
                )
            }
            else -> throw IllegalArgumentException("No delegate defined for ${this::class.simpleName}")
        }
    }
}
