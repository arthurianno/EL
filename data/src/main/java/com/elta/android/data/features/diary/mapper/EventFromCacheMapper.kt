package com.elta.android.data.features.diary.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.dto.event.ActivityTypeDto
import com.elta.android.data.features.diary.dto.event.EventDataDto
import com.elta.android.data.features.diary.dto.event.EventDto
import com.elta.android.data.features.diary.dto.event.EventTypeDto
import com.elta.android.data.features.diary.dto.event.InsulinTypeDto
import com.elta.android.data.features.diary.dto.event.MealTagDto
import com.nullgr.core.date.CommonFormats.FORMAT_STANDARD_DATE_FULL_MILLIS_UTC
import com.nullgr.core.date.toStringWithFormat
import com.nullgr.core.date.toTimestamp
import javax.inject.Inject

class EventFromCacheMapper @Inject constructor() : Mapper<EventCachedDto, EventDto> {

    override fun mapFromObject(source: EventCachedDto): EventDto =
        with(source) {
            EventDto(
                id = secondaryId,
                data = EventDataDto(
                    duration = duration,
                    value = value,
                    kind = kind,
                    name = name,
                    activityType = activityType?.let { ActivityTypeDto.valueOf(it) },
                    mealTag = mealTag?.let { MealTagDto.valueOf(it) },
                    insulinType = insulinType?.let { InsulinTypeDto.valueOf(it) },
                    type = EventTypeDto.valueOf(type)
                ),
                additionTime = additionTime.toStringWithFormat(FORMAT_STANDARD_DATE_FULL_MILLIS_UTC),
                tagId = tagId,
                note = note,
                modificationTime = modificationTime?.toTimestamp(),
                state = StateDto.valueOf(state)
            )
        }
}