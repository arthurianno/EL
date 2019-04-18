package com.elta.android.domain.features.reminder.model

import java.util.Date

fun Reminder.isChanged(
    title: String? = null,
    date: Date? = null,
    schedule: ScheduleType? = null
): Boolean =
    this.title != title ||
        this.time != date ||
        this.scheduleType != schedule