package com.elta.android.domain.features.reminder.model

import org.threeten.bp.ZonedDateTime

fun Reminder.isChanged(
    title: String? = null,
    date: ZonedDateTime? = null,
    schedule: ScheduleType? = null
): Boolean =
    this.title != title ||
        this.time != date ||
        this.scheduleType != schedule