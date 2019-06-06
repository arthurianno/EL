package com.elta.android.presentation.features.profile.settings.reminders.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.reminder.model.ScheduleType
import org.threeten.bp.ZonedDateTime
import timber.log.Timber

private const val ID = "com.elta.android.reminder_id"
private const val TIME = "com.elta.android.reminder_time"
private const val TITLE = "com.elta.android.reminder_title"
private const val TYPE = "com.elta.android.reminder_type"

fun Reminder.toPendingIntent(context: Context): PendingIntent =
    PendingIntent.getBroadcast(
        context,
        id.hashCode(),
        getIntent(context),
        PendingIntent.FLAG_UPDATE_CURRENT
    ).also {
        Timber.tag("Reminder").d(it.toString())
    }

fun getCancelPendingIntent(context: Context, id: String): PendingIntent =
    PendingIntent.getBroadcast(
        context,
        id.hashCode(),
        Intent(context, ReminderNotificationReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

fun Reminder.getIntent(context: Context) =
    Intent(context, ReminderNotificationReceiver::class.java).apply {
        putExtra(ID, id)
        putExtra(TIME, time)
        putExtra(TITLE, title)
        putExtra(TYPE, scheduleType)
    }.also {
        Timber.tag("Reminder").d(it.extras.toString())
    }

fun Intent.getReminder(): Reminder =
    Reminder(
        id = getStringExtra(ID),
        time = getSerializableExtra(TIME) as ZonedDateTime,
        title = getStringExtra(TITLE),
        scheduleType = getSerializableExtra(TYPE) as ScheduleType
    )