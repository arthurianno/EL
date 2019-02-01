package com.elta.android.presentation.features.onboaring.ui.adapter.delegates

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingWeightItem
import com.elta.android.presentation.utils.withAdapterPosition
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.adapter.ktx.AdapterDelegate
import com.nullgr.core.adapter.ktx.ViewHolder
import com.nullgr.core.rx.RxBus
import kotlinx.android.synthetic.main.item_onboarding_weight.*

class OnBoardingWeightDelegate(
    private val bus: RxBus
) : AdapterDelegate() {

    override val layoutResource: Int = R.layout.item_onboarding_weight
    override val itemType: Any = OnBoardingWeightItem::class

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return super.onCreateViewHolder(parent).apply {
            with(this as ViewHolder) {
                weightView.valueChanges().subscribe { newValue ->
                    withAdapterPosition<OnBoardingWeightItem> { _, item, _ ->
                        item.value = newValue
                        bus.event(Events.WeightSelected(item.value))
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(items: List<ListItem>, position: Int, holder: RecyclerView.ViewHolder) {
        val item = items[position] as OnBoardingWeightItem

        with(holder as ViewHolder) {
            if (item.initialValue != null && item.value == null) {
                weightView.setValue(item.initialValue)
            }
        }
    }
}