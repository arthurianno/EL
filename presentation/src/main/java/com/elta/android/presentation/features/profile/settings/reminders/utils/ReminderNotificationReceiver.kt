package com.elta.android.presentation.features.profile.settings.reminders.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.elta.android.presentation.core.notification.NotificationSource
import com.elta.android.presentation.jobs.RemindersManager
import dagger.android.AndroidInjection
import javax.inject.Inject

class ReminderNotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: NotificationSource

    @Inject
    lateinit var remindersManager: RemindersManager

    override fun onReceive(context: Context?, intent: Intent) {
        if (intent.action?.contains(ACTION_NOTIFICATION) == true) {
            AndroidInjection.inject(this, context)
            val reminder = intent.getReminder()
            notificationManager.sendNotification(
                title = reminder.title,
                text = reminder.title,
                id = reminder.id
            )
            remindersManager.updateReminder(reminder)
        }
    }
}