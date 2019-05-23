package com.elta.android.presentation.features.profile.settings.reminders.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.elta.android.presentation.core.notification.NotificationSource
import com.elta.android.presentation.jobs.ReminderWorker
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

class ReminderNotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationManager: NotificationSource

    @Inject
    lateinit var reminderManager: ReminderWorker

    override fun onReceive(context: Context?, intent: Intent) {
        AndroidInjection.inject(this, context)
        val reminder = intent.getReminder()
        Timber.tag("Reminder").d("notification ${reminder.title}")
        notificationManager.sendNotification(
            title = reminder.title,
            text = reminder.title,
            id = reminder.id
        )
        reminderManager.updateReminder(reminder)
    }
}