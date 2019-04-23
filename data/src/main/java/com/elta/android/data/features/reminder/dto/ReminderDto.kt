package com.elta.android.data.features.reminder.dto

import java.util.Date

data class ReminderDto(
    val id: String,
    val title: String,
    val time: Date,
    val scheduleType: ScheduleTypeDto
)