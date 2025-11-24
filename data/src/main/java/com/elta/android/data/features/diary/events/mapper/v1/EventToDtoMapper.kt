package com.elta.android.data.features.diary.events.mapper.v1

import com.elta.android.common.mapper.Mapper
import com.elta.android.common.utils.toIsoString
import com.elta.android.data.features.calculator.mapper.toNetwork
import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.events.dto.ActivityTypeDto
import com.elta.android.data.features.diary.events.dto.v1.EventDataDto
import com.elta.android.data.features.diary.events.dto.v1.EventDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto
import com.elta.android.data.features.diary.events.dto.EventTypeDto.Companion.toEventTypeDto
import com.elta.android.data.features.diary.events.dto.v1.InsulinMedicamentDataDto
import com.elta.android.data.features.diary.events.dto.v1.InsulinTypeDto
import com.elta.android.data.features.diary.events.dto.MealTagDto
import com.elta.android.data.features.diary.events.extensions.countOrZero
import com.elta.android.domain.features.diary.events.model.Event
import javax.inject.Inject

class EventToDtoMapper @Inject constructor() : Mapper<Event, EventDto> {

    override fun mapFromObject(source: Event): EventDto =
        with(source) {
            val eventType = type.toEventTypeDto()
            EventDto(
                id = id,
                state = StateDto.valueOf(state.name),
                additionTime = additionTime.toIsoString(),
                tagId = tagId,
                note = note,
                modificationTime = modificationTime,
                data = EventDataDto(
                    type = eventType,
                    value = value,
                    kind = kind,
                    name = name,
                    temperature = temperature,
                    duration = duration,
                    activityType = activityType?.let { ActivityTypeDto.valueOf(it.name) },
                    mealTag = mealTag?.let { MealTagDto.valueOf(it.name) },
                    insulinType = insulinType?.let { InsulinTypeDto.valueOf(it.name) },
                    insulinMedicament = InsulinMedicamentDataDto(medicament = medicament),
                    glucometerSerialNumber = glucometerSerialNumber,
                    products = dishes.toNetwork(eventType),
                    productsCount = if (eventType == EventTypeDto.BREAD) dishes.countOrZero() else null,
                )
            )
        }
}