package com.elta.android.presentation.features.profile.settings.reminders.base.model

import com.elta.android.domain.features.reminder.model.ScheduleType
import java.util.Date

data class ReminderFormModel(
    var inputValue: String? = null,
    var date: Date? = null,
    var isDateChanged: Boolean = false,
    var schedule: ScheduleType? = null
)