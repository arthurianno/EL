package com.elta.android.presentation

import com.elta.android.presentation.core.ui.dialog.DialogData
import com.nullgr.core.resources.ResourceProvider

private const val EMPTY_STRING = ""

sealed class Dialogs : DialogData {

    data class ExitAndLoseData(
        val resources: ResourceProvider,
        override val title: String = resources.getString(R.string.event_form_dialog_title),
        override val message: String = resources.getString(R.string.event_form_exit_dialog_body),
        override val negative: String = resources.getString(R.string.event_form_dialog_cancel_button),
        override val positive: String = resources.getString(R.string.event_form_exit_dialog_confirm_button)
    ) : Dialogs()

    data class GooglePlayRateData(
        val resources: ResourceProvider,
        override val title: String = resources.getString(R.string.google_play_rate_dialog_title),
        override val message: String = resources.getString(R.string.google_play_rate_dialog_body),
        override val negative: String = resources.getString(R.string.google_play_rate_dialog_negative_button),
        override val positive: String = resources.getString(R.string.google_play_rate_dialog_positive_button)
    ) : Dialogs()

    data class LikeAppRateData(
        val resources: ResourceProvider,
        val step: Int,
        override val message: String = resources.getString(R.string.like_app_dialog_body),
        override val negative: String = resources.getString(R.string.like_app_dialog_negative_button),
        override val positive: String = resources.getString(R.string.like_app_dialog_positive_button)
    ) : Dialogs() {
        override val title: String
            get() = resources.getString(R.string.like_app_dialog_title, step)
    }

    data class FeedbackData(
        val resources: ResourceProvider,
        override val title: String = resources.getString(R.string.feedback_dialog_title),
        override val message: String = resources.getString(R.string.feedback_dialog_body),
        override val negative: String = resources.getString(R.string.feedback_dialog_negative_button),
        override val positive: String = resources.getString(R.string.feedback_dialog_positive_button)
    ) : Dialogs()

    data class EventDelete(
        val resources: ResourceProvider,
        override val title: String = resources.getString(R.string.event_form_dialog_title),
        override val message: String = resources.getString(R.string.event_form_delete_dialog_body),
        override val negative: String = resources.getString(R.string.event_form_dialog_cancel_button),
        override val positive: String = resources.getString(R.string.event_form_delete_dialog_confirm_button)
    ) : Dialogs()

    data class EventUnlinkNetwork(
        val resources: ResourceProvider,
        override val title: String = resources.getString(R.string.profile_unlink_network_dialog_title),
        override val message: String = resources.getString(R.string.profile_unlink_network_dialog_body),
        override val negative: String = resources.getString(R.string.profile_unlink_network_cancel_button),
        override val positive: String = resources.getString(R.string.profile_unlink_network_dialog_confirm_button)
    ) : Dialogs()

    data class EventDeleteReminder(
        val resources: ResourceProvider,
        override val title: String = resources.getString(R.string.profile_delete_reminder_dialog_title),
        override val message: String = resources.getString(R.string.profile_delete_reminder_dialog_body),
        override val negative: String = resources.getString(R.string.profile_delete_reminder_cancel_button),
        override val positive: String = resources.getString(R.string.profile_delete_reminder_dialog_confirm_button)
    ) : Dialogs()

    data class EventDeleteObserver(
        val resources: ResourceProvider,
        override val title: String = resources.getString(R.string.profile_delete_observer_dialog_title),
        override val message: String = resources.getString(R.string.profile_delete_observer_dialog_body),
        override val negative: String = resources.getString(R.string.profile_delete_observer_cancel_button),
        override val positive: String = resources.getString(R.string.profile_delete_observer_dialog_confirm_button)
    ) : Dialogs()

    data class DeleteDevice(
        val resources: ResourceProvider,
        val isPrimary: Boolean,
        override val title: String = resources.getString(R.string.profile_delete_device_dialog_title),
        override val negative: String = resources.getString(R.string.profile_delete_observer_cancel_button),
        override val positive: String = resources.getString(R.string.profile_delete_device_dialog_confirm_button)
    ) : Dialogs() {
        override val message: String
            get() = resources.getString(
                if (isPrimary) R.string.profile_delete_primary_device_dialog_body
                else R.string.profile_delete_device_dialog_body
            )
    }

    data class GoogleFitActivated(
        val resources: ResourceProvider,
        override val title: String = resources.getString(R.string.profile_google_fit_activated_title),
        override val negative: String? = null,
        override val positive: String = resources.getString(R.string.profile_google_fit_activated_positive_button),
        override val message: String = resources.getString(R.string.profile_google_fit_activated_message)
    ) : Dialogs()

    data class DeleteProfile(
        val resources: ResourceProvider,
        override val title: String = resources.getString(R.string.profile_delete_title),
        override val message: String = resources.getString(R.string.profile_delete_text),
        override val negative: String = resources.getString(R.string.profile_delete_button_dissmit),
        override val positive: String = resources.getString(R.string.profile_delete_button_confirm)
    ) : Dialogs()

    data class ProfileIsDeleted(
        val resources: ResourceProvider,
        override val title: String = resources.getString(R.string.profile_is_deleted_title),
        override val message: String = resources.getString(R.string.profile_is_deleted_body),
        override val negative: String? = resources.getString(R.string.profile_support_email_description),
        override val positive: String? = resources.getString(R.string.ok)
    ) : Dialogs()

    data class ChangeBreadUnitsData(
        val resourceProvider: ResourceProvider,
        override val title: String? = null,
        override val message: String = resourceProvider.getString(R.string.calculator_user_change_bread_units_message),
        override val negative: String? = null,
        override val positive: String? = resourceProvider.getString(R.string.ok)
    ) : Dialogs()

    data class SettingsLocationDialogData(
        val resourceProvider: ResourceProvider,
        override val title: String = resourceProvider.getString(R.string.settings_dialog_title),
        override val message: String = resourceProvider.getString(R.string.location_dialog_message),
        override val negative: String = resourceProvider.getString(R.string.settings_dialog_negative),
        override val positive: String = resourceProvider.getString(R.string.settings_dialog_positive),
    ) : Dialogs()

    data class SettingsBluetoothDialogData(
        val resourceProvider: ResourceProvider,
        override val title: String = resourceProvider.getString(R.string.settings_dialog_title),
        override val message: String = resourceProvider.getString(R.string.bluetooth_dialog_message),
        override val negative: String = resourceProvider.getString(R.string.settings_dialog_negative),
        override val positive: String = resourceProvider.getString(R.string.settings_dialog_positive),
    ) : Dialogs()

    data class DeviceAlreadyConnectedDialogData(
        val resourceProvider: ResourceProvider,
        override val title: String? = null,
        override val message: String = resourceProvider.getString(R.string.device_connected_dialog_message),
        override val negative: String? = null,
        override val positive: String = resourceProvider.getString(R.string.ok),
    ) : Dialogs()

    data class EditingXEIsNotAvailableData(
        val resourceProvider: ResourceProvider,
        override val title: String? = resourceProvider.getString(R.string.event_form_dialog_title),
        override val message: String = resourceProvider.getString(R.string.calculator_editing_XE_is_not_available),
        override val negative: String? = null,
        override val positive: String? = resourceProvider.getString(R.string.ok),
    ) : Dialogs()

    sealed class Emias : Dialogs() {
        data class UserConnected(
            val resourceProvider: ResourceProvider,
            override val title: String = resourceProvider.getString(R.string.emias_user_connected_title),
            override val message: String = resourceProvider.getString(R.string.emias_user_connected_message),
            override val negative: String? = null,
            override val positive: String = resourceProvider.getString(R.string.emias_dialog_confirm)
        ) : Emias()

        data class UserNotFound(
            val resourceProvider: ResourceProvider,
            override val title: String = resourceProvider.getString(R.string.emias_user_not_found_title),
            override val message: String = resourceProvider.getString(R.string.emias_user_not_found_message),
            override val negative: String? = null,
            override val positive: String = resourceProvider.getString(R.string.emias_dialog_confirm)
        ) : Emias()

        data class BadInternetConnection(
            val resourceProvider: ResourceProvider,
            override val title: String = resourceProvider.getString(R.string.emias_network_connection_error_title),
            override val message: String = resourceProvider.getString(R.string.emias_network_connection_error_message),
            override val negative: String? = null,
            override val positive: String = resourceProvider.getString(R.string.emias_dialog_confirm)
        ) : Emias()

        data class AgreementNotFound(
            val resourceProvider: ResourceProvider,
            override val title: String = resourceProvider.getString(R.string.emias_agreement_not_found_title),
            override val message: String = resourceProvider.getString(R.string.emias_agreement_not_found_message),
            override val negative: String? = null,
            override val positive: String = resourceProvider.getString(R.string.emias_dialog_confirm)
        ) : Emias()

        data class InternalError(
            val resourceProvider: ResourceProvider,
            override val title: String = resourceProvider.getString(R.string.emias_internal_error_title),
            override val message: String = EMPTY_STRING,
            override val negative: String? = null,
            override val positive: String = resourceProvider.getString(R.string.emias_dialog_confirm)
        ) : Emias()

        data class UserAlreadyLinked(
            val resourceProvider: ResourceProvider,
            override val title: String = resourceProvider.getString(R.string.emias_oms_already_linked_title),
            override val message: String = resourceProvider.getString(R.string.emias_oms_already_linked_message),
            override val negative: String? = null,
            override val positive: String = resourceProvider.getString(R.string.emias_dialog_confirm)
        ) : Emias()

        data class ReceivingDataError(
            val resourceProvider: ResourceProvider,
            override val title: String = resourceProvider.getString(R.string.emias_receiving_data_error_title),
            override val message: String = resourceProvider.getString(R.string.emias_try_later_message),
            override val negative: String? = null,
            override val positive: String = resourceProvider.getString(R.string.emias_dialog_confirm)
        ) : Emias()

        data class NoInternetConnection(
            val resourceProvider: ResourceProvider,
            override val title: String = resourceProvider.getString(R.string.emias_no_internet_connection_error_title),
            override val message: String = resourceProvider.getString(R.string.emias_try_later_message),
            override val negative: String? = null,
            override val positive: String = resourceProvider.getString(R.string.emias_dialog_confirm)
        ) : Emias()
    }

    data class ReminderWrongTime(
        val resourceProvider: ResourceProvider,
        override val title: String? = null,
        override val message: String = resourceProvider.getString(R.string.profile_reminders_wrong_time),
        override val negative: String? = null,
        override val positive: String? = resourceProvider.getString(R.string.ok),
    ) : Dialogs()

    data class DataPickerExit(
        val resourceProvider: ResourceProvider,
        override val title: String? = null,
        override val message: String = resourceProvider.getString(R.string.date_picker_exit_message),
        override val negative: String? = resourceProvider.getString(R.string.no_text),
        override val positive: String? = resourceProvider.getString(R.string.yes_text),
    ) : Dialogs()

}
