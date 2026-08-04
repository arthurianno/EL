package com.elta.android.data.features.diary.events.mapper.v2

import com.elta.android.common.mapper.Mapper
import com.elta.android.common.utils.toIsoString
import com.elta.android.data.features.calculator.mapper.toNetwork
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.events.dto.ActivityTypeDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto.Companion.toEventTypeDto
import com.elta.android.data.features.diary.events.dto.GlucoseInputTypeDto.Companion.toDto
import com.elta.android.data.features.diary.events.dto.MealTagDto
import com.elta.android.data.features.diary.events.dto.v2.EventDataV2Dto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import com.elta.android.data.features.diary.events.extensions.countOrZero
import com.elta.android.data.features.diary.events.mapper.toDto
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.MealTag
import javax.inject.Inject

class EventV2ToDtoMapper @Inject constructor() : Mapper<EventV2, EventV2Dto> {

    override fun mapFromObject(source: EventV2): EventV2Dto =
        with(source) {
            val eventType = type.toEventTypeDto()
            val tabletsNumberForRequest = if (eventType == EventTypeDto.MEDICAMENTS) {
                tabletsNumber ?: DEFAULT_MEDICAMENT_TABLETS_NUMBER
            } else {
                tabletsNumber
            }
            EventV2Dto(
                id = id,
                state = StateDto.valueOf(state.name),
                additionTime = additionTime.toIsoString(),
                tagId = tagId,
                note = note,
                modificationTime = modificationTime,
                data = EventDataV2Dto(
                    type = eventType,
                    value = value,
                    kind = kind,
                    name = name,
                    temperature = temperature,
                    duration = duration,
                    activityType = activityType?.let { ActivityTypeDto.valueOf(it.name) },
                    mealTag = mealTag?.let { MealTagDto.valueOf(it.name) },
                    insulinMedicament = insulinMedicament?.toDto(),
                    medicament = medicament?.toDto(),
                    tabletsNumber = tabletsNumberForRequest,
                    glucometerSerialNumber = glucometerSerialNumber,
                    products = dishes.toNetwork(eventType),
                    productsCount = if (eventType == EventTypeDto.BREAD) dishes.countOrZero() else null,
                    inputType = glucoseInputType?.toDto(),
                    isTimeInvalid = isTimeInvalid,
                    isTemperatureInvalid = isTemperatureInvalid
                )
            )
        }

    private companion object {
        const val DEFAULT_MEDICAMENT_TABLETS_NUMBER = 1.0
    }
}
