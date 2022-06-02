package com.elta.android.presentation.utils

import com.elta.android.domain.features.reminder.model.ScheduleType
import com.elta.android.presentation.R
import com.nullgr.core.resources.ResourceProvider

fun ScheduleType.toString(resources: ResourceProvider): String =
    when (this) {
        ScheduleType.NONE -> resources.getString(R.string.profile_reminders_schedule_not_repeat)
        ScheduleType.DAY -> resources.getString(R.string.profile_reminders_schedule_day)
        ScheduleType.WEEK -> resources.getString(R.string.profile_reminders_schedule_week)
        ScheduleType.MONTH -> resources.getString(R.string.profile_reminders_schedule_month)
        ScheduleType.YEAR -> resources.getString(R.string.profile_reminders_schedule_year)
    }
