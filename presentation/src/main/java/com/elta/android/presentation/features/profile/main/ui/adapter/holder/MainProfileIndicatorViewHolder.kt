package com.elta.android.presentation.features.profile.main.ui.adapter.holder

import com.elta.android.presentation.Clicks
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemProfileIndicatorsBinding
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileIndicatorItem
import com.nullgr.core.rx.RxBus

class MainProfileIndicatorViewHolder(
    private val binding: ItemProfileIndicatorsBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<MainProfileIndicatorItem>(binding.root) {
    override fun bind(item: MainProfileIndicatorItem) {
        with(binding) {
            glucoseLevelMinView.text = item.glucoseLevelMin
            glucoseLevelMaxView.text = item.glucoseLevelMax
            diabetesTypeButtonView.text = item.diabetesType
            weightValueView.text = item.weight
            hemoglobinValueView.text = item.hemoglobin
            glucoseLevelView.setOnClickListener {
                bus.click(Clicks.ProfileIndicatorClicked(MainProfileIndicatorItem.Type.GLUCOSE_LEVEL))
            }
            diabetesTypeView.setOnClickListener {
                bus.click(Clicks.ProfileIndicatorClicked(MainProfileIndicatorItem.Type.DIABETES))
            }
            weightView.setOnClickListener {
                bus.click(Clicks.ProfileIndicatorClicked(MainProfileIndicatorItem.Type.WEIGHT))
            }
            hemoglobinView.setOnClickListener {
                bus.click(Clicks.ProfileIndicatorClicked(MainProfileIndicatorItem.Type.HEMOGLOBIN))
            }
        }
    }
}
