package com.elta.android.data.features.diary.events.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.devices.glucometer.toStorageDateTime
import com.elta.android.domain.features.diary.events.model.Event
import com.elta.android.domain.features.diary.events.model.EventType
import com.elta.android.domain.features.diary.events.model.State
import com.elta.android.domain.features.user.interactor.round
import javax.inject.Inject

class EventFromGlucometerMapper @Inject constructor() : Mapper<GlucometerEventDto, Event> {

    override fun mapFromObject(source: GlucometerEventDto): Event =
        with(source) {
            Event(
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
                temperature = temperature.toCelsius().round(1),
                duration = null,
                activityType = null,
                mealTag = null,
                insulinType = null,
                medicament = null,
                state = State.CREATED
            )
        }

    private fun Int?.toCelsius(): Double =
        if (this == null) {
            0.0
        } else {
            this - ABSOLUTE_ZERO
        }

    companion object {
        private const val ABSOLUTE_ZERO = 273.15
    }
}
