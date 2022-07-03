package com.elta.android.presentation.features.profile.main.ui.adapter.holder

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemProfileFunctionsBinding
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileAdditionalItem
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.toggleView

class MainProfileAdditionalViewHolder(
    private val binding: ItemProfileFunctionsBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<MainProfileAdditionalItem>(binding.root) {
    override fun bind(item: MainProfileAdditionalItem) {
        with(binding) {
            functionIconView.setImageResource(item.icon)
            functionNameView.setText(item.title)
            item.description?.let { functionDescriptionNameView.setText(it) }
            functionDescriptionNameView.toggleView(item.description != null)
            functionStateView.toggleView(item.showGoArrow)
            functionView.setOnClickListener {
                bus.click(Clicks.ProfileAdditionalClicked(item))
            }
        }
    }
}
