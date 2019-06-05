package com.elta.android.data.features.reminder.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.common.utils.toIsoDate
import com.elta.android.data.features.reminder.cache.dto.ReminderCacheDto
import com.elta.android.data.features.reminder.dto.ReminderDto
import com.elta.android.data.features.reminder.dto.ScheduleTypeDto
import javax.inject.Inject

class ReminderFromCacheMapper @Inject constructor() : Mapper<ReminderCacheDto, ReminderDto> {

    override fun mapFromObject(source: ReminderCacheDto): ReminderDto =
        with(source) {
            ReminderDto(
                id = secondaryId,
                title = title,
                time = time.toIsoDate(),
                scheduleType = ScheduleTypeDto.valueOf(schedule)
            )
        }
}