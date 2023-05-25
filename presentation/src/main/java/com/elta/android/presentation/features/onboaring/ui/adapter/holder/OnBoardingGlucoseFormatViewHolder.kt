package com.elta.android.presentation.features.onboaring.ui.adapter.holder

import com.elta.android.domain.features.user.model.GlucoseFormat
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemOnboardingGlucoseFormatBinding
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingGlucoseFormatItem
import com.nullgr.core.rx.RxBus

class OnBoardingGlucoseFormatViewHolder(
    private val binding: ItemOnboardingGlucoseFormatBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<OnBoardingGlucoseFormatItem>(binding.root) {
    override fun bind(item: OnBoardingGlucoseFormatItem) {
        if (item.format == GlucoseFormat.CAPILLARY) {
            binding.caplilary.isSelected = true
        } else {
            binding.plasma.isSelected = true
        }
        binding.caplilary.setOnClickListener {
            item.format = GlucoseFormat.CAPILLARY
            bus.event(Events.OnBoardingPageSelected(item))
            it.isSelected = true
            binding.plasma.isSelected = false
        }
        binding.plasma.setOnClickListener {
            item.format = GlucoseFormat.PLASMA
            bus.event(Events.OnBoardingPageSelected(item))
            it.isSelected = true
            binding.caplilary.isSelected = false
        }
    }
}
