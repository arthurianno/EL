package com.elta.android.data.features.diary.events.mapper.v1

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.events.cache.dto.v1.EventCachedDto
import com.elta.android.data.features.diary.events.dto.ActivityTypeDto
import com.elta.android.data.features.diary.events.dto.v1.EventDataDto
import com.elta.android.data.features.diary.events.dto.v1.EventDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.v1.InsulinMedicamentDataDto
import com.elta.android.data.features.diary.events.dto.v1.InsulinTypeDto
import com.elta.android.data.features.diary.events.dto.MealTagDto
import com.elta.android.data.features.diary.events.extensions.countOrZero
import com.elta.android.data.features.diary.events.mapper.toProductsList
import javax.inject.Inject

class EventFromCacheMapper @Inject constructor() : Mapper<EventCachedDto, EventDto> {

    override fun mapFromObject(source: EventCachedDto): EventDto =
        with(source) {
            val productsList = products.toProductsList()
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
                    type = EventTypeDto.valueOf(type),
                    insulinMedicament = InsulinMedicamentDataDto(medicament = medicament),
                    glucometerSerialNumber = glucometerSerialNumber,
                    products = productsList,
                    productsCount = productsList.countOrZero()
                ),
                additionTime = additionTimeString,
                tagId = tagId,
                note = note,
                modificationTime = modificationTime,
                state = StateDto.valueOf(state)
            )
        }
}
