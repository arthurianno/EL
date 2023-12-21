package com.elta.android.presentation.notifications

import android.app.AlarmManager
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat

fun areNotificationsEnabled(notificationManager: NotificationManagerCompat): Boolean =
    when {
        notificationManager.areNotificationsEnabled().not() -> false
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
            notificationManager.notificationChannels
                .firstOrNull { channel ->
                    channel.importance == NotificationManager.IMPORTANCE_NONE
                } == null
        }
        else -> true
    }

fun areAlarmsAndRemindersEnabled(alarmManager: AlarmManager): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }