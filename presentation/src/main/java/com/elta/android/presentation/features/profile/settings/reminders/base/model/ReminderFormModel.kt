package com.elta.android.presentation.features.profile.settings.reminders.base.model

import com.elta.android.domain.features.reminder.model.ScheduleType
import org.threeten.bp.ZonedDateTime

data class ReminderFormModel(
    var inputValue: String? = null,
    var date: ZonedDateTime? = null,
    var isDateChanged: Boolean = false,
    var schedule: ScheduleType? = null
)
