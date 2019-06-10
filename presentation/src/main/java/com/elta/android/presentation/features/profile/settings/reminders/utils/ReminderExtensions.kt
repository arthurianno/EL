package com.elta.android.presentation.features.profile.settings.reminders.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.elta.android.common.utils.toIsoDate
import com.elta.android.common.utils.toIsoString
import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.reminder.model.ScheduleType

const val ACTION_SPENT = "com.elta.android.reminder.SPENT"
const val ACTION_CANCEL = "com.elta.android.reminder.CANCEL"

private const val ID = "com.elta.android.reminder_id"
private const val TIME = "com.elta.android.reminder_time"
private const val TITLE = "com.elta.android.reminder_title"
private const val TYPE = "com.elta.android.reminder_type"

fun Reminder.getPendingIntent(context: Context): PendingIntent =
    PendingIntent.getBroadcast(
        context.applicationContext,
        id.hashCode(),
        getIntent(context.applicationContext),
        PendingIntent.FLAG_UPDATE_CURRENT
    )

fun getCancelPendingIntent(context: Context, id: String): PendingIntent =
    PendingIntent.getBroadcast(
        context,
        id.hashCode(),
        Intent(context, ReminderCallbackReceiver::class.java).apply {
            action = ACTION_CANCEL
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )

fun Reminder.getIntent(context: Context) =
    Intent(context, ReminderCallbackReceiver::class.java).apply {
        action = "$ACTION_SPENT$id"
        putExtra(ID, id)
        putExtra(TIME, date.toIsoString())
        putExtra(TITLE, title)
        putExtra(TYPE, scheduleType.name)
    }

fun Intent.getReminder(): Reminder =
    Reminder(
        id = getStringExtra(ID),
        date = getStringExtra(TIME).toIsoDate(),
        title = getStringExtra(TITLE),
        scheduleType = getStringExtra(TYPE).let { ScheduleType.valueOf(it) }
    )