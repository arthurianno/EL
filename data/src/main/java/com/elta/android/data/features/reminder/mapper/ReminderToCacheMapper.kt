package com.elta.android.data.features.reminder.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.reminder.cache.dto.ReminderCacheDto
import com.elta.android.data.features.reminder.dto.ReminderDto
import javax.inject.Inject

class ReminderToCacheMapper @Inject constructor() : Mapper<ReminderDto, ReminderCacheDto> {

    override fun mapFromObject(source: ReminderDto): ReminderCacheDto =
        with(source) {
            ReminderCacheDto(
                id = id.hashCode().toLong(),
                secondaryId = id,
                title = title,
                time = time,
                schedule = scheduleType.name
            )
        }
}