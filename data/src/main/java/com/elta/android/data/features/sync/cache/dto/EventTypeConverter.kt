package com.elta.android.data.features.sync.cache.dto

import com.elta.android.data.features.diary.events.dto.EventTypeDto
import io.objectbox.converter.PropertyConverter

class EventTypeConverter : PropertyConverter<String, String> {
    private val eventTypes = EventTypeDto.values().map { it.name }

    override fun convertToEntityProperty(databaseValue: String?): String =
        getEventTypeNameFrom(databaseValue)

    override fun convertToDatabaseValue(entityProperty: String?): String =
        getEventTypeNameFrom(entityProperty)

    private fun getEventTypeNameFrom(value: String?) =
        value
            ?.findAnyOf(strings = eventTypes, ignoreCase = true)
            ?.second
            .orEmpty()
}
