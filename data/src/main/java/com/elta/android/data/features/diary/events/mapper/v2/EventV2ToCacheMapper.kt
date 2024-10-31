package com.elta.android.data.features.diary.events.mapper.v2

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.diary.events.cache.dto.v2.EventV2CachedDto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import com.elta.android.data.features.diary.events.extensions.toAdditionMillis
import com.elta.android.data.features.diary.events.mapper.toCache
import javax.inject.Inject

class EventV2ToCacheMapper @Inject constructor() : Mapper<EventV2Dto, EventV2CachedDto> {

    override fun mapFromObject(source: EventV2Dto): EventV2CachedDto =
        with(source) {
            EventV2CachedDto(
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
                medicament = data.insulinMedicament,
                medicamentDto = data.medicament,
                tabletsNumber = data.tabletsNumber,
                temperature = data.temperature,
                state = state.name,
                glucometerSerialNumber = data.glucometerSerialNumber,
                products = data.products.toCache(),
                glucoseInputType = data.inputType
            )
        }
}
