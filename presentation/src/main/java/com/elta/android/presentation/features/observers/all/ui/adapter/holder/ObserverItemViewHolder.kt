package com.elta.android.presentation.features.observers.all.ui.adapter.holder

import android.view.View
import com.elta.android.domain.features.observers.model.ObserverStatus
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemProfileAdditionalSettingsBinding
import com.elta.android.presentation.features.observers.all.ui.adapter.items.ObserverItem
import com.nullgr.core.rx.RxBus

class ObserverItemViewHolder(
    private val binding: ItemProfileAdditionalSettingsBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<ObserverItem>(binding.root) {
    override fun bind(item: ObserverItem) {
        with(binding) {
            settingsTypeIconView.setImageResource(item.type)
            settingsActionIconView.setImageResource(item.action)
            settingsNameView.text = item.title

            when (item.status) {
                ObserverStatus.CONFIRMED -> {
                    settingsActionIconView.visibility = View.VISIBLE
                    itemView.isClickable = true
                }
                else -> {
                    settingsActionIconView.visibility = View.GONE
                    itemView.isClickable = false
                }
            }
            settingsDescriptionNameView.text = item.description
            root.setOnClickListener {
                bus.click(Clicks.ObserverItemClicked(item))
            }
        }
    }
}
