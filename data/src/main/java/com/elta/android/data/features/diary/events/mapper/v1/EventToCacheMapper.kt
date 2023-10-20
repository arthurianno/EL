package com.elta.android.data.features.diary.events.mapper.v1

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.diary.events.cache.dto.v1.EventCachedDto
import com.elta.android.data.features.diary.events.dto.v1.EventDto
import com.elta.android.data.features.diary.events.extensions.toAdditionMillis
import com.elta.android.data.features.diary.events.mapper.toCache
import javax.inject.Inject

class EventToCacheMapper @Inject constructor() : Mapper<EventDto, EventCachedDto> {

    override fun mapFromObject(source: EventDto): EventCachedDto =
        with(source) {
            EventCachedDto(
                id = id.hashCode().toLong(),
                secondaryId = id,
                type = data.type.name,
                additionTime = additionTime.toAdditionMillis(),
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
                medicament = data.insulinMedicament?.medicament,
                temperature = data.temperature,
                state = state.name,
                glucometerSerialNumber = data.glucometerSerialNumber,
                products = data.products.toCache()
            )
        }
}
