package com.elta.android.presentation.features.onboaring.ui.adapter.holder

import android.view.View
import android.widget.TextView
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemOnboardingDiabetesTypesBinding
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingDiabetesItem
import com.elta.android.presentation.utils.toString
import com.nullgr.core.resources.ResourceProvider
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.children

class OnBoardingDiabetesViewHolder(
    private val binding: ItemOnboardingDiabetesTypesBinding,
    private val bus: RxBus,
    private val resources: ResourceProvider
) : BaseListItemViewHolder<OnBoardingDiabetesItem>(binding.root) {
    override fun bind(item: OnBoardingDiabetesItem) {
        with(binding) {
            item.types.forEachIndexed { index, type ->
                val child = typesView.getChildAt(index) as TextView
                child.text = type.toString(resources)
                child.tag = type
                child.isSelected = type == item.type
                val listener = View.OnClickListener { view ->
                    val newType = view.tag as Diabetes
                    switchDiabetesType(item, newType)
                    binding.typesView.children().forEach { child ->
                        child.isSelected = child.tag as Diabetes == item.type
                    }
                    bus.event(Events.OnBoardingPageSelected(item))
                }

                binding.typesView.children().forEach {
                    it.setOnClickListener(listener)
                }
            }
        }
    }

    private fun switchDiabetesType(item: OnBoardingDiabetesItem, newType: Diabetes) {
        if (newType == item.type) {
            item.type = null
        } else {
            item.type = newType
        }
    }
}
