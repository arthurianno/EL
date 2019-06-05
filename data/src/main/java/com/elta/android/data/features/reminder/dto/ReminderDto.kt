package com.elta.android.data.features.reminder.dto

import org.threeten.bp.ZonedDateTime

data class ReminderDto(
    val id: String,
    val title: String,
    val time: ZonedDateTime,
    val scheduleType: ScheduleTypeDto
)