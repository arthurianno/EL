package com.elta.android.domain.features.reminder.model

import org.threeten.bp.ZonedDateTime

data class Reminder(
    val id: String,
    val title: String,
    var time: ZonedDateTime,
    val scheduleType: ScheduleType
)