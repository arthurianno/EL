package com.elta.android.presentation.features.profile.main.ui.adapter.holder

import android.util.Log
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
                Log.i("NavTrace", "MainProfileIndicatorViewHolder click(type=GLUCOSE_LEVEL)")
                bus.click(Clicks.ProfileIndicatorClicked(MainProfileIndicatorItem.Type.GLUCOSE_LEVEL))
            }
            diabetesTypeView.setOnClickListener {
                Log.i("NavTrace", "MainProfileIndicatorViewHolder click(type=DIABETES)")
                bus.click(Clicks.ProfileIndicatorClicked(MainProfileIndicatorItem.Type.DIABETES))
            }
            weightView.setOnClickListener {
                Log.i("NavTrace", "MainProfileIndicatorViewHolder click(type=WEIGHT)")
                bus.click(Clicks.ProfileIndicatorClicked(MainProfileIndicatorItem.Type.WEIGHT))
            }
            hemoglobinView.setOnClickListener {
                Log.i("NavTrace", "MainProfileIndicatorViewHolder click(type=HEMOGLOBIN)")
                bus.click(Clicks.ProfileIndicatorClicked(MainProfileIndicatorItem.Type.HEMOGLOBIN))
            }
        }
    }
}
