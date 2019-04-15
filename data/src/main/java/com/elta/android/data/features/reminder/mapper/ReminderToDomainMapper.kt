package com.elta.android.data.features.reminder.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.reminder.dto.ReminderDto
import com.elta.android.domain.features.reminder.model.PeriodicType
import com.elta.android.domain.features.reminder.model.Reminder
import javax.inject.Inject

class ReminderToDomainMapper @Inject constructor() : Mapper<ReminderDto, Reminder> {

    override fun mapFromObject(source: ReminderDto): Reminder =
        with(source) {
            Reminder(
                id = id,
                time = time,
                title = title,
                periodic = PeriodicType.valueOf(periodic.name)
            )
        }
}