package com.elta.android.presentation.features.onboaring.ui.adapter.items

import com.elta.android.domain.features.user.model.Diabetes
import com.nullgr.core.adapter.items.ListItem

data class OnBoardingDiabetesItem(
    override val title: String,
    val types: List<Diabetes>
) : ListItem, OnBoardingItem {

    var type: Diabetes? = null
}