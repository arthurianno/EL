package com.elta.android.presentation.features.profile.settings.reminders.all.model

import com.elta.android.presentation.core.compose.common.Action

sealed class RemindersAction : Action {
    object CreateReminder : RemindersAction()
    object OpenCreateReminder : RemindersAction()
    data class OpenReminder(val id: String) : RemindersAction()
    data class PermissionResult(val isGranted: Boolean) : RemindersAction()
    object OpenSettingsDialog : RemindersAction()
}
