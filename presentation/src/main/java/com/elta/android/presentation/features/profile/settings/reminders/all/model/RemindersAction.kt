package com.elta.android.presentation.features.profile.settings.reminders.all.model

import com.elta.android.presentation.core.compose.common.Action

sealed class RemindersAction : Action {
    object CreateReminder : RemindersAction()
    object OpenCreateReminder : RemindersAction()
    data class OpenReminder(val id: String) : RemindersAction()
    data class NotificationPermissionResult(val isGranted: Boolean) : RemindersAction()
    data class AlarmsAndRemindersPermissionResult(val isGranted: Boolean) : RemindersAction()
    object OpenNotificationSettingsDialog : RemindersAction()
    object OpenAlarmsAndRemindersSettingsDialog : RemindersAction()
}
