package com.elta.android.data.features.reminder.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.reminder.dto.ScheduleTypeDto
import com.elta.android.data.features.reminder.dto.ReminderDto
import com.elta.android.domain.features.reminder.model.Reminder
import javax.inject.Inject

class ReminderToDtoMapper @Inject constructor() : Mapper<Reminder, ReminderDto> {

    override fun mapFromObject(source: Reminder): ReminderDto =
        with(source) {
            ReminderDto(
                id = id,
                time = date,
                title = title,
                scheduleType = ScheduleTypeDto.valueOf(scheduleType.name)
            )
        }
}