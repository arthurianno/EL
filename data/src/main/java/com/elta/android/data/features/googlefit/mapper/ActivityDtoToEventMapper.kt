package com.elta.android.data.features.googlefit.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.common.utils.toMillisUtc
import com.elta.android.common.utils.toZonedDateTime
import com.elta.android.data.features.common.storage.UserHolder
import com.elta.android.data.features.googlefit.dto.ActivityDto
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.State
import java.util.UUID
import javax.inject.Inject

class ActivityDtoToEventMapper @Inject constructor(
    private val activityTypeMapper: Mapper<String, ActivityType>,
    private val userHolder: UserHolder
) : Mapper<ActivityDto, Event> {

    override fun mapFromObject(source: ActivityDto): Event =
        Event(
            id = UUID.nameUUIDFromBytes("${source.id}${userHolder.currentUser}".toByteArray()).toString(),
            additionTime = source.additionTime.toZonedDateTime(),
            tagId = null,
            tag = null,
            note = null,
            modificationTime = source.additionTime.toZonedDateTime().toMillisUtc(),
            value = null,
            name = null,
            kind = null,
            temperature = null,
            duration = source.duration,
            activityType = activityTypeMapper.mapFromObject(source.activityType),
            insulinType = null,
            type = EventType.ACTIVITY,
            mealTag = null,
            state = State.CREATED
        )
}
