package com.elta.android.data.features.diary.events.mapper.v2

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.diary.events.dto.v1.EventDto
import com.elta.android.data.features.diary.events.dto.SimpleEventDto
import com.elta.android.data.features.diary.events.dto.v2.EventV2Dto
import javax.inject.Inject

class EventV2ToSimpleMapper @Inject constructor() : Mapper<EventV2Dto, SimpleEventDto> {
    override fun mapFromObject(source: EventV2Dto): SimpleEventDto =
        with(source) {
            SimpleEventDto(
                id = id,
                type = data.type
            )
        }
}
