package com.elta.android.presentation.features.onboaring.ui.adapter.holder

import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemOnboardingWeightBinding
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingWeightItem
import com.nullgr.core.rx.RxBus
import java.util.concurrent.TimeUnit

private const val INTERVAL = 300L

class OnBoardingWeightViewHolder(
    private val binding: ItemOnboardingWeightBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<OnBoardingWeightItem>(binding.root) {
    override fun bind(item: OnBoardingWeightItem) {
        if (item.initialValue != null && item.weight == null) {
            binding.weightView.setValue(item.initialValue)
        }
        binding.weightView.valueChanges().throttleLast(INTERVAL, TimeUnit.MILLISECONDS)
            .subscribe { newValue ->
                item.weight = newValue
                bus.event(Events.OnBoardingPageSelected(item))
            }
    }
}
