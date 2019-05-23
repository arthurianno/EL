package com.elta.android.data.features.sync.cache.dto

import com.elta.android.data.features.common.dto.StateDto
import io.objectbox.converter.PropertyConverter

class StateDtoConverter : PropertyConverter<StateDto, String> {
    override fun convertToDatabaseValue(entityProperty: StateDto) = entityProperty.toString()

    override fun convertToEntityProperty(databaseValue: String) = StateDto.valueOf(databaseValue)
}