package com.elta.android.presentation.features.onboaring.ui.adapter.items

import com.nullgr.core.adapter.items.ListItem

interface OnBoardingItem : ListItem {
    val title: String
    val data: Any?
}