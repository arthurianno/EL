package com.elta.android.data.features.diary.events.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.events.dto.ActivityTypeDto
import com.elta.android.data.features.diary.events.dto.EventDataDto
import com.elta.android.data.features.diary.events.dto.EventDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.InsulinTypeDto
import com.elta.android.data.features.diary.events.dto.MealTagDto
import javax.inject.Inject

class EventFromCacheMapper @Inject constructor() : Mapper<EventCachedDto, EventDto> {

    override fun mapFromObject(source: EventCachedDto): EventDto =
        with(source) {
            EventDto(
                id = secondaryId,
                data = EventDataDto(
                    temperature = temperature,
                    duration = duration,
                    value = value,
                    kind = kind,
                    name = name,
                    activityType = activityType?.let { ActivityTypeDto.valueOf(it) },
                    mealTag = mealTag?.let { MealTagDto.valueOf(it) },
                    insulinType = insulinType?.let { InsulinTypeDto.valueOf(it) },
                    type = EventTypeDto.valueOf(type)
                ),
                additionTime = additionTimeString,
                tagId = tagId,
                note = note,
                modificationTime = modificationTime,
                state = StateDto.valueOf(state)
            )
        }
}