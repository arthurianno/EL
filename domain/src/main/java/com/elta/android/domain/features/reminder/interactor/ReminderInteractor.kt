package com.elta.android.domain.features.reminder.interactor

import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.reminder.model.ScheduleType
import org.threeten.bp.ZonedDateTime

fun List<Reminder>.sortByTime(): List<Reminder> = sortedBy { it.date }

fun getNextReminderDate(
    type: ScheduleType,
    reminderDate: ZonedDateTime,
    now: ZonedDateTime
): ZonedDateTime? {
    return if (reminderDate.isAfter(now)) reminderDate
    else when (type) {
        ScheduleType.NONE -> null
        ScheduleType.DAY ->
            reminderDate
                .withYear(now.year)
                .withMonth(now.monthValue)
                .withDayOfMonth(now.dayOfMonth)
                .plusDays(1)
        ScheduleType.WEEK ->
            reminderDate
                .withYear(now.year)
                .withMonth(now.monthValue)
                .withDayOfMonth(now.dayOfMonth)
                .plusDays(7)
        ScheduleType.MONTH ->
            reminderDate
                .withYear(now.year)
                .withMonth(now.monthValue)
                .plusMonths(1)
        ScheduleType.YEAR ->
            reminderDate
                .withYear(now.year)
                .plusYears(1)
    }?.apply {
        withSecond(0)
        withNano(0)
    }
}

fun getNextReminder(reminder: Reminder): Reminder? =
    getNextReminderDate(
        reminder.scheduleType,
        reminder.date,
        ZonedDateTime.now()
    )?.let { reminder.copy(date = it) }

fun Reminder.isInThePast(): Boolean = date.isBefore(ZonedDateTime.now())
fun Reminder.isOneTime(): Boolean = scheduleType == ScheduleType.NONE
