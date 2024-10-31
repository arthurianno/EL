package com.elta.android.data.features.diary.events.cache.converter

import com.elta.android.data.features.diary.events.dto.GlucoseInputTypeDto
import io.objectbox.converter.PropertyConverter

class GlucoseInputTypeDtoConverter : PropertyConverter<GlucoseInputTypeDto, String> {

    override fun convertToEntityProperty(databaseValue: String?): GlucoseInputTypeDto? {
        return databaseValue?.let { value ->
            GlucoseInputTypeDto.valueOf(value)
        }
    }

    override fun convertToDatabaseValue(entityProperty: GlucoseInputTypeDto?): String? {
        return entityProperty?.name
    }
}
