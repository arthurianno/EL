package com.elta.android.presentation

import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.domain.features.user.model.Profile
import com.elta.android.presentation.core.bus.Event
import com.elta.android.presentation.features.main.events.chooser.models.ChooserResult
import com.elta.android.presentation.features.onboaring.ui.adapter.items.OnBoardingItem

sealed class Events : Event {

    data class OnBoardingPageSelected(val item: OnBoardingItem) : Events()
    data class HomeModelChanged(val model: HomeModel) : Events()
    data class HomeBottomSheetStateChanged(val opened: Boolean) : Events()
    data class RecordsAttachedStateChanged(val attached: Boolean) : Events()
    data class ChooserVariantSelected(val chooserResult: ChooserResult) : Events()
    data class ChooserTagSelected(val chooserResult: ChooserResult) : Events()
    object EventsChanged : Events()
    data class ProfileChanged(val profile: Profile) : Events()
    object ReminderDeleted : Events()
    object ProfileUpdated : Events()
    data class PinCodeEntered(val pin: String): Events()
}