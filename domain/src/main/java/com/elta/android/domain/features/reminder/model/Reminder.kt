package com.elta.android.domain.features.reminder.model

import java.util.Date

data class Reminder(
    val id: String,
    val title: String,
    var time: Date,
    val scheduleType: ScheduleType
)