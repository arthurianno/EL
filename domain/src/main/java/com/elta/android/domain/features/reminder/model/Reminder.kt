package com.elta.android.domain.features.reminder.model

import java.util.Date

data class Reminder(
    val id: String,
    val title: String,
    val time: Date,
    val scheduleType: ScheduleType
)