package com.elta.android.presentation.features.onboaring.ui.adapter.items

data class OnBoardingWeightItem(
    override val title: String,
    val initialValue: Double? = null
) : OnBoardingItem {

    override val data: Any?
        get() = weight ?: initialValue

    var weight: Double? = null
}
