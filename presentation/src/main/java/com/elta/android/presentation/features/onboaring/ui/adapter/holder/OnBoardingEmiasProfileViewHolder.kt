package com.elta.android.presentation.features.onboaring.ui.adapter.holder

import com.elta.android.presentation.Events
import com.elta.android.presentation.core.bus.event
import com.elta.android.presentation.core.ui.adapter.BaseListItemViewHolder
import com.elta.android.presentation.databinding.ItemOnboardingEmiasProfileBinding
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingEmiasProfileItem
import com.elta.android.presentation.features.onboaring.ui.adapter.validators.BirthdayValidator
import com.elta.android.presentation.features.onboaring.ui.adapter.validators.OMSValidator
import com.nullgr.core.resources.ResourceProvider
import com.nullgr.core.rx.RxBus


class OnBoardingEmiasProfileViewHolder (
    private val binding: ItemOnboardingEmiasProfileBinding,
    private val bus: RxBus,
    private val resources: ResourceProvider
) : BaseListItemViewHolder<OnBoardingEmiasProfileItem>(binding.root) {
    override fun bind(item: OnBoardingEmiasProfileItem) {

        val birthdayValidator = BirthdayValidator(binding.dateBirthInputView, resources) { (value, isValid) ->
            item.birthday = value
            item.birthdayIsValid = isValid
            bus.event(Events.OnBoardingPageSelected(item))
        }
        birthdayValidator.validate()

        val omsValidator = OMSValidator(binding.omsInputView, resources) { (value, isValid) ->
            item.oms = value
            item.omsIsValid = isValid
            bus.event(Events.OnBoardingPageSelected(item))
        }
        omsValidator.validate()
    }

}



