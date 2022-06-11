package com.elta.android.domain.features.reminder.model

import org.threeten.bp.ZonedDateTime

data class Reminder(
    val id: String,
    val title: String,
    val date: ZonedDateTime,
    val scheduleType: ScheduleType
)
