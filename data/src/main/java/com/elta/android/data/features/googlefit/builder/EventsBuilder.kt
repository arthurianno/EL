package com.elta.android.data.features.googlefit.builder

import com.elta.android.common.mapper.Mapper
import com.elta.android.common.utils.toZonedDateTime
import com.elta.android.data.features.googlefit.dto.ActivityDto
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.State
import java.util.UUID
import javax.inject.Inject

class EventsBuilder @Inject constructor(
    private val activityTypeMapper: Mapper<String, ActivityType>
) {
    fun buildEvents(activities: List<ActivityDto>, profileEmail: String): List<EventV2> =
        activities.map { mapFromObject(it, profileEmail) }

    private fun mapFromObject(source: ActivityDto, email: String): EventV2 =
        EventV2(
            id = UUID.nameUUIDFromBytes("${source.id}$email".toByteArray()).toString(),
            additionTime = source.additionTime.toZonedDateTime(),
            tagId = null,
            tag = null,
            note = source.note,
            modificationTime = null,
            value = null,
            name = null,
            kind = null,
            temperature = null,
            duration = source.duration,
            activityType = activityTypeMapper.mapFromObject(source.activityType),
            medicament = null,
            type = EventType.ACTIVITY,
            mealTag = null,
            state = State.CREATED,
            glucometerSerialNumber = null,
            dishes = emptyList()
        )
}
