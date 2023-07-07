package com.elta.android.presentation

import android.net.Uri
import com.elta.android.domain.features.diary.home.model.HomeModel
import com.elta.android.domain.features.reminder.model.Reminder
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
    data class ChooserVariantWithSubtypesSelected(val chooserResult: ChooserResult) : Events()
    data class ChooserTagSelected(val chooserResult: ChooserResult) : Events()
    data class EventsChanged(val isCreated: Boolean) : Events()
    data class ProfileChanged(val profile: Profile) : Events()
    object ShouldUpdateProfile : Events()
    object ReminderChanged : Events()
    object ReminderDeleted : Events()
    object ProfileUpdated : Events()
    object ObserverInvited : Events()
    object ObserversUpdated : Events()
    object ProfileDataChanged : Events()
    data class PinCodeEntered(val pin: String) : Events()
    object DeviceChanged : Events()
    object FirmwareUpdated : Events()
    data class BackendSyncProgress(val inProgress: Boolean) : Events()
    object BootCompleted : Events()
    object PackageReplaced : Events()
    data class ReminderSpent(val reminder: Reminder) : Events()
    data class ReportLoadedEvent(val uri: Uri) : Events()

    sealed class Sync : Events() {
        sealed class Glucometer : Sync() {
            object Started : Glucometer()
            object ErrorWithMessage : Glucometer()
            object Error : Glucometer()
            object Success : Glucometer()
            object Nothing : Glucometer()
        }

        sealed class Server : Sync() {
            object Started : Server()
            object Error : Server()
            object Success : Server()
        }
    }
}
