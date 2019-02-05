package com.elta.android.presentation.features.onboaring.ui.adapter.items

import com.elta.android.domain.features.user.model.Gender

data class OnBoardingGenderItem(
    override val title: String
) : OnBoardingItem {

    override val data: Any?
        get() = gender

    var gender: Gender? = null
}