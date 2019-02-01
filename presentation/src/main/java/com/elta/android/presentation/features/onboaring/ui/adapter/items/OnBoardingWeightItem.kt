package com.elta.android.presentation.features.onboaring.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

data class OnBoardingWeightItem(
    override val title: String,
    val initialValue: Double? = null
) : ListItem, OnBoardingItem {

    var value: Double? = null
}