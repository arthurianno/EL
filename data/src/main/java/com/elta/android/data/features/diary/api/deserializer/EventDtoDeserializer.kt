package com.elta.android.data.features.diary.api.deserializer

import com.elta.android.data.features.common.dto.StateDto
import com.elta.android.data.features.diary.dto.EventDataDto
import com.elta.android.data.features.diary.dto.EventTypeDto
import com.elta.android.data.features.diary.dto.event.ActivityDataDto
import com.elta.android.data.features.diary.dto.event.BreadDataDto
import com.elta.android.data.features.diary.dto.event.EventDto
import com.elta.android.data.features.diary.dto.event.GlucoseDataDto
import com.elta.android.data.features.diary.dto.event.InsulinDataDto
import com.elta.android.data.features.diary.dto.event.MedicamentsDataDto
import com.elta.android.data.features.diary.dto.event.WeightDataDto
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class EventDtoDeserializer : JsonDeserializer<EventDto> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): EventDto {
        val jsonObject = json.asJsonObject

        val id = jsonObject["id"].asString

        val dataElement = jsonObject["data"]
        val dataObject = dataElement.asJsonObject
        val typeElement = dataObject["eventType"]

        val type = context.deserialize<EventTypeDto>(typeElement, EventTypeDto::class.java) as EventTypeDto
        val data = context.deserialize<EventDataDto>(dataElement, type.toEventDataDtoClass()) as EventDataDto

        val addedTime = jsonObject["additionalTime"].asString
        val tagId = jsonObject["tag"]?.asString
        val note = jsonObject["note"]?.asString
        val timeStamp = jsonObject["timeStamp"]?.asLong

        val stateElement = jsonObject["state"]
        val state = context.deserialize<StateDto>(stateElement, StateDto::class.java) as StateDto

        return EventDto(
            id = id,
            data = data,
            additionTime = addedTime,
            tagId = tagId,
            note = note,
            modificationTime = timeStamp,
            state = state
        )
    }

    private fun EventTypeDto.toEventDataDtoClass(): Class<out EventDataDto> =
        when (this) {
            EventTypeDto.BREAD -> BreadDataDto::class.java
            EventTypeDto.INSULIN -> InsulinDataDto::class.java
            EventTypeDto.MEDICAMENTS -> MedicamentsDataDto::class.java
            EventTypeDto.ACTIVITY -> ActivityDataDto::class.java
            EventTypeDto.WEIGHT -> WeightDataDto::class.java
            EventTypeDto.GLUCOSE -> GlucoseDataDto::class.java
        }
}