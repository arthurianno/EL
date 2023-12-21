package com.elta.android.presentation.features.onboaring.ui.adapter.holder

import android.content.Context
import android.view.View
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemOnboardingDiabetesTypesBinding
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingDiabetesItem
import com.elta.android.presentation.features.profile.settings.dialogs.diabetes.extension.createDiabetesButtonView
import com.elta.android.presentation.utils.toStringRes
import com.nullgr.core.rx.RxBus
import com.nullgr.core.ui.extensions.children

class OnBoardingDiabetesViewHolder(
    private val binding: ItemOnboardingDiabetesTypesBinding,
    private val bus: RxBus,
    private val context: Context,
) : BaseListItemViewHolder<OnBoardingDiabetesItem>(binding.root) {
    override fun bind(item: OnBoardingDiabetesItem) {
        item.types.forEach { diabetes ->
            val textView = createDiabetesButtonView(context)
                .apply {
                    setText(diabetes.toStringRes())
                    tag = diabetes
                    isSelected = diabetes == item.type

                    val listener = View.OnClickListener { _ ->
                        switchDiabetesType(item, diabetes)
                        binding.typesView.children().forEach { child ->
                            child.isSelected = child.tag as Diabetes == item.type
                        }
                        bus.event(Events.OnBoardingPageSelected(item))
                    }
                    setOnClickListener(listener)
                }
            binding.typesView.addView(textView)
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
