package com.elta.android.presentation.features.home.ui.adapter

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemUserEventBinding
import com.elta.android.presentation.features.home.ui.adapter.items.UserEventItem
import com.nullgr.core.rx.RxBus

internal class UserEventViewHolder(
    private val binding: ItemUserEventBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<UserEventItem>(binding.root) {
    override fun bind(item: UserEventItem) {
        with(binding) {
            eventIconView.setImageResource(item.iconRes)
            eventTitleView.setText(item.titleRes)
            root.setOnClickListener {
                bus.click(Clicks.AddUserEvent(item.meta))
            }
        }
    }
}
