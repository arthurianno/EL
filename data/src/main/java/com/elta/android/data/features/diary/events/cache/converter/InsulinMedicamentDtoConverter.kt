package com.elta.android.data.features.diary.events.cache.converter

import com.elta.android.data.features.diary.events.dto.v2.InsulinMedicamentDto
import com.google.gson.Gson
import io.objectbox.converter.PropertyConverter

class InsulinMedicamentDtoConverter : PropertyConverter<InsulinMedicamentDto, String> {

    override fun convertToEntityProperty(databaseValue: String?): InsulinMedicamentDto? {
        return databaseValue?.let { value ->
            Gson().fromJson(value, InsulinMedicamentDto::class.java)
        }
    }

    override fun convertToDatabaseValue(entityProperty: InsulinMedicamentDto?): String? {
        return entityProperty?.let { property ->
            Gson().toJson(property)
        }
    }
}