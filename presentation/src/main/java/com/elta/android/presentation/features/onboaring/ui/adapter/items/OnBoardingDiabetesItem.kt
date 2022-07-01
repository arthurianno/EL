package com.elta.android.presentation.features.onboaring.ui.adapter.items

import com.elta.android.domain.features.user.model.Diabetes

data class OnBoardingDiabetesItem(
    override val title: String,
    val types: List<Diabetes>
) : OnBoardingItem {

    override val data: Any?
        get() = type

    var type: Diabetes? = null
}
