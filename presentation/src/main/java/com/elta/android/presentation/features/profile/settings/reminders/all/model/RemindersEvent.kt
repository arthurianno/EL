package com.elta.android.presentation.features.profile.settings.reminders.all.model

import com.elta.android.presentation.core.compose.common.Event

sealed class RemindersEvent: Event {
    override fun equals(other: Any?) = false
    override fun hashCode() = System.identityHashCode(this)

    object ShowDeleteReminder : RemindersEvent()
    object CheckNotificationPermission : RemindersEvent()
    object OpenSettings : RemindersEvent()
}