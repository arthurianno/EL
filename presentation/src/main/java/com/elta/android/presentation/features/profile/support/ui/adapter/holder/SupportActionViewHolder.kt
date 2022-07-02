package com.elta.android.presentation.features.profile.support.ui.adapter.holder

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemSupportActionBinding
import com.elta.android.presentation.features.profile.support.ui.adapter.items.SupportActionItem
import com.nullgr.core.rx.RxBus

class SupportActionViewHolder(
    private val binding: ItemSupportActionBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<SupportActionItem>(binding.root) {
    override fun bind(item: SupportActionItem) {
        with(binding) {
            actionIconView.setImageResource(item.icon)
            actionNameView.text = item.title
            actionDescriptionNameView.text = item.subTitle
            actionView.setOnClickListener {
                bus.click(Clicks.SupportActionClicked(item.action))
            }
        }
    }
}
