package com.elta.android.data.features.diary.events.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.common.utils.toIsoDate
import com.elta.android.data.features.diary.events.dto.EventDto
import com.elta.android.domain.features.diary.events.model.ActivityType
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.InsulinType
import com.elta.android.domain.features.diary.events.model.MealTag
import com.elta.android.domain.features.diary.events.model.State
import javax.inject.Inject

class EventToDomainMapper @Inject constructor() : Mapper<EventDto, Event> {

    override fun mapFromObject(source: EventDto): Event =
        with(source) {
            Event(
                id = id,
                type = EventType.valueOf(data.type.name),
                additionTime = additionTime.toIsoDate(),
                tagId = tagId,
                tag = null,
                note = note,
                modificationTime = modificationTime,
                value = data.value,
                kind = data.kind,
                name = data.name,
                temperature = data.temperature,
                duration = data.duration,
                activityType = data.activityType?.let { ActivityType.valueOf(it.name) },
                mealTag = data.mealTag?.let { MealTag.valueOf(it.name) },
                insulinType = data.insulinType?.let { InsulinType.valueOf(it.name) },
                medicament = data.insulinMedicament?.medicament,
                state = State.valueOf(state.name),
                glucometerSerialNumber = data.glucometerSerialNumber
            )
        }
}
