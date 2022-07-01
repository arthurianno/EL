package com.elta.android.presentation.features.profile.main.ui.adapter.delegates

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.presentation.Clicks
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.click
import com.elta.android.presentation.databinding.ItemProfileIndicatorsBinding
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileIndicatorItem
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileIndicatorItem.Payload
import com.elta.android.presentation.features.profile.main.ui.adapter.items.MainProfileIndicatorItem.Type
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.rx.RxBus

class MainProfileIndicatorDelegate(
    private val bus: RxBus
) : AdapterDelegate<ItemProfileIndicatorsBinding>(ItemProfileIndicatorsBinding::inflate) {

    override val itemType = MainProfileIndicatorItem::class
    override val layoutResource = R.layout.item_profile_indicators

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(binding) {
                glucoseLevelView.setOnClickListener {
                    bus.click(Clicks.ProfileIndicatorClicked(Type.GLUCOSE_LEVEL))
                }
                diabetesTypeView.setOnClickListener {
                    bus.click(Clicks.ProfileIndicatorClicked(Type.DIABETES))
                }
                weightView.setOnClickListener {
                    bus.click(Clicks.ProfileIndicatorClicked(Type.WEIGHT))
                }
                hemoglobinView.setOnClickListener {
                    bus.click(Clicks.ProfileIndicatorClicked(Type.HEMOGLOBIN))
                }
            }
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder
    ) {
        val item = items[position] as MainProfileIndicatorItem
        with(binding) {
            glucoseLevelMinView.text = item.glucoseLevelMin
            glucoseLevelMaxView.text = item.glucoseLevelMax
            diabetesTypeButtonView.text = item.diabetesType
            weightValueView.text = item.weight
            hemoglobinValueView.text = item.hemoglobin
        }
    }

    override fun onBindViewHolder(
        items: List<ListItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payload: Any
    ) {
        val item = items[position] as MainProfileIndicatorItem
        with(binding) {
            when (payload) {
                Payload.GLUCOSE_LEVEL_CHANGED -> {
                    glucoseLevelMinView.text = item.glucoseLevelMin
                    glucoseLevelMaxView.text = item.glucoseLevelMax
                }
                Payload.DIABETES_CHANGED -> diabetesTypeButtonView.text = item.diabetesType
                Payload.WEIGHT_CHANGED -> weightValueView.text = item.weight
                Payload.HEMOGLOBIN_CHANGED -> hemoglobinValueView.text = item.hemoglobin
            }
        }
    }
}
