package com.elta.android.data.features.diary.events.mapper.v2

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.devices.glucometer.toStorageDateTime
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.EventV2
import com.elta.android.domain.features.diary.events.model.State
import javax.inject.Inject

class EventV2FromGlucometerMapper @Inject constructor() : Mapper<GlucometerEventDto, EventV2> {

    override fun mapFromObject(source: GlucometerEventDto): EventV2 =
        with(source) {
            EventV2(
                id = id,
                type = EventType.GLUCOSE,
                additionTime = checkNotNull(date).toStorageDateTime(),
                tagId = null,
                tag = null,
                note = null,
                modificationTime = null,
                value = value,
                kind = null,
                name = null,
                temperature = temperature,
                duration = null,
                activityType = null,
                mealTag = null,
                medicament = null,
                state = State.CREATED,
                glucometerSerialNumber = glucometerSerialNumber,
                dishes = emptyList()
            )
        }
}
