package com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.holder

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemHemoglobinBinding
import com.elta.android.presentation.features.profile.settings.dialogs.hemoglobin.ui.adapter.items.HemoglobinItem
import com.nullgr.core.rx.RxBus

class HemoglobinViewHolder(
    private val binding: ItemHemoglobinBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<HemoglobinItem>(binding.root) {
    override fun bind(item: HemoglobinItem) {
        with(binding) {
            valueTextView.text = item.value
            dateTextView.text = item.date
            deleteButtonView.setOnClickListener {
                bus.click(Clicks.DeleteHemoglobinEventClicked(item.id))
            }
        }
    }
}
