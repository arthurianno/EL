package com.elta.android.data.features.diary.events.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.common.utils.toIsoDate
import com.elta.android.common.utils.toMillisUtc
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.events.dto.EventDto
import javax.inject.Inject

class EventToCacheMapper @Inject constructor() : Mapper<EventDto, EventCachedDto> {

    override fun mapFromObject(source: EventDto): EventCachedDto =
        with(source) {
            EventCachedDto(
                id = id.hashCode().toLong(),
                secondaryId = id,
                type = data.type.name,
                additionTime = additionTime.toIsoDate().toMillisUtc(),
                additionTimeString = additionTime,
                tagId = tagId,
                note = note,
                modificationTime = modificationTime,
                value = data.value,
                kind = data.kind,
                name = data.name,
                duration = data.duration,
                activityType = data.activityType?.name,
                mealTag = data.mealTag?.name,
                insulinType = data.insulinType?.name,
                temperature = data.temperature,
                state = state.name
            )
        }
}