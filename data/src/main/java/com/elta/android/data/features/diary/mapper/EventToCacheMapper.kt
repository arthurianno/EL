package com.elta.android.data.features.diary.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.diary.cache.dto.EventCachedDto
import com.elta.android.data.features.diary.dto.event.EventDto
import com.nullgr.core.date.CommonFormats.FORMAT_STANDARD_DATE_FULL_MILLIS_UTC
import com.nullgr.core.date.dateFromTimestamp
import com.nullgr.core.date.toDate
import java.util.Date
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
                additionTime = additionTime.toDate(FORMAT_STANDARD_DATE_FULL_MILLIS_UTC) as Date,
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