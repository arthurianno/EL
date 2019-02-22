package com.elta.android.data.features.diary.events.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.common.getDate
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.diary.events.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.events.dto.EventDto
import com.nullgr.core.date.dateFromTimestamp
import javax.inject.Inject

class EventToCacheMapper @Inject constructor(
    private val userHolder: UserHolder
) : Mapper<EventDto, EventCachedDto> {

    override fun mapFromObject(source: EventDto): EventCachedDto =
        with(source) {
            EventCachedDto(
                id = id.hashCode().toLong(),
                secondaryId = id,
                userId = userHolder.currentUser,
                type = data.type.name,
                additionTime = additionTime.getDate(),
                tagId = tagId,
                note = note,
                modificationTime = modificationTime?.dateFromTimestamp(),
                value = data.value,
                kind = data.kind,
                name = data.name,
                duration = data.duration,
                activityType = data.activityType?.name,
                mealTag = data.mealTag?.name,
                insulinType = data.insulinType?.name,
                state = state.name
            )
        }
}