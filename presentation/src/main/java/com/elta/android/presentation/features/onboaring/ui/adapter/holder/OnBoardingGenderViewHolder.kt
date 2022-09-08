package com.elta.android.presentation.features.onboaring.ui.adapter.holder

import android.view.View
import com.elta.android.domain.features.user.model.Gender
import com.elta.android.presentation.Events
import com.elta.android.presentation.R
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemOnboardingGenderBinding
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingGenderItem
import com.nullgr.core.rx.RxBus

class OnBoardingGenderViewHolder(
    private val binding: ItemOnboardingGenderBinding,
    private val bus: RxBus
) : BaseListItemViewHolder<OnBoardingGenderItem>(binding.root) {
    override fun bind(item: OnBoardingGenderItem) {
        with(binding) {
            setGendersState(genderMaleView, genderFemaleView, item.gender)
            val listener = View.OnClickListener { view ->
                val newGender = when (view.id) {
                    R.id.genderMaleView -> Gender.MALE
                    else -> Gender.FEMALE
                }
                switchGenders(item, newGender)
                setGendersState(genderMaleView, genderFemaleView, item.gender)
                bus.event(Events.OnBoardingPageSelected(item))
            }

            genderMaleView.setOnClickListener(listener)
            genderFemaleView.setOnClickListener(listener)
        }
    }

    private fun setGendersState(genderMale: View, genderFemale: View, gender: Gender?) {
        genderMale.isSelected = Gender.MALE == gender
        genderFemale.isSelected = Gender.FEMALE == gender
    }

    private fun switchGenders(item: OnBoardingGenderItem, newGender: Gender) {
        if (newGender == item.gender) {
            item.gender = null
        } else {
            item.gender = newGender
        }
    }
}
