package com.elta.android.data.features.diary.events.mapper.v2

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.events.cache.dto.v2.EventV2CachedDto
import com.elta.android.data.features.diary.events.dto.ActivityTypeDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.MealTagDto
import com.elta.android.data.features.diary.events.dto.v2.EventDataV2Dto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import com.elta.android.data.features.diary.events.extensions.countOrZero
import com.elta.android.data.features.diary.events.mapper.toProductsList
import javax.inject.Inject

class EventV2FromCacheMapper @Inject constructor() : Mapper<EventV2CachedDto, EventV2Dto> {

    override fun mapFromObject(source: EventV2CachedDto): EventV2Dto =
        with(source) {
            val productsList = products.toProductsList()
            EventV2Dto(
                id = secondaryId,
                data = EventDataV2Dto(
                    temperature = temperature,
                    duration = duration,
                    value = value,
                    kind = kind,
                    name = name,
                    activityType = activityType?.let { ActivityTypeDto.valueOf(it) },
                    mealTag = mealTag?.let { MealTagDto.valueOf(it) },
                    insulinMedicament = medicament,
                    medicament = medicamentDto,
                    tabletsNumber = tabletsNumber,
                    type = EventTypeDto.valueOf(type),
                    glucometerSerialNumber = glucometerSerialNumber,
                    products = productsList,
                    productsCount = productsList.countOrZero(),
                    inputType = source.glucoseInputType,
                    isTimeInvalid = source.isTimeInvalid,
                    isTemperatureInvalid = source.isTemperatureInvalid
                ),
                additionTime = additionTimeString,
                tagId = tagId,
                note = note,
                modificationTime = modificationTime,
                state = StateDto.valueOf(state)
            )
        }
}
