package com.elta.android.presentation

import com.elta.android.presentation.core.bus.Event
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingItem

sealed class Events : Event {

    data class OnBoardingPageSelected(val item: OnBoardingItem) : Events()
}