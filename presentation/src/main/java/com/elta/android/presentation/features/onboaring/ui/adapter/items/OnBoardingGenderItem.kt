package com.elta.android.presentation.features.onboaring.ui.adapter.items

import com.elta.android.domain.features.user.model.Gender
import com.nullgr.core.adapter.items.ListItem

data class OnBoardingGenderItem(val title: String) : ListItem {
    var gender: Gender? = null
}