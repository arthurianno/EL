package com.elta.android.data.features.diary.events.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.diary.events.dto.EventDto
import com.elta.android.data.features.diary.events.dto.SimpleEventDto
import javax.inject.Inject

class EventToSimpleMapper @Inject constructor() : Mapper<EventDto, SimpleEventDto> {
    override fun mapFromObject(source: EventDto): SimpleEventDto =
        with(source) {
            SimpleEventDto(
                id = id,
                type = data.type
            )
        }
}
