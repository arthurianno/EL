package com.elta.android.domain.features.reminder.interactor

import com.elta.android.domain.features.reminder.model.Reminder
import com.elta.android.domain.features.reminder.model.ScheduleType
import org.threeten.bp.ZonedDateTime

fun List<Reminder>.sortByTime(): List<Reminder> = sortedBy { it.date }

inline fun getNextReminderDate(type: ScheduleType, reminderDate: ZonedDateTime, now: ZonedDateTime): ZonedDateTime? =
    when (type) {
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

inline fun getNextReminder(reminder: Reminder): Reminder? =
    getNextReminderDate(reminder.scheduleType, reminder.date, ZonedDateTime.now())?.let { reminder.copy(date = it) }

inline fun Reminder.isNeedToUpdate(): Boolean =
    date.isBefore(ZonedDateTime.now()) && scheduleType != ScheduleType.NONE

inline fun Reminder.isInPast(): Boolean = date.isBefore(ZonedDateTime.now())
inline fun Reminder.isOneTime(): Boolean = scheduleType == ScheduleType.NONE
