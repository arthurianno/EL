package com.elta.android.data.features.reminder.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.reminder.cache.dto.ReminderCacheDto
import com.elta.android.data.features.reminder.dto.ScheduleTypeDto
import com.elta.android.data.features.reminder.dto.ReminderDto
import javax.inject.Inject

class ReminderFromCacheMapper @Inject constructor() : Mapper<ReminderCacheDto, ReminderDto> {

    override fun mapFromObject(source: ReminderCacheDto): ReminderDto =
        with(source) {
            ReminderDto(
                id = secondaryId,
                title = title,
                time = time,
                scheduleType = ScheduleTypeDto.valueOf(schedule)
            )
        }
}