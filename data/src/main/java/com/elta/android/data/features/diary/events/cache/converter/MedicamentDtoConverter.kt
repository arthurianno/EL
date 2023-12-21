package com.elta.android.data.features.diary.events.cache.converter

import com.elta.android.data.features.diary.events.dto.v2.MedicamentDto
import com.google.gson.Gson
import io.objectbox.converter.PropertyConverter

class MedicamentDtoConverter : PropertyConverter<MedicamentDto, String> {

    override fun convertToEntityProperty(databaseValue: String?): MedicamentDto? {
        databaseValue ?: return null
        return Gson().fromJson(databaseValue, MedicamentDto::class.java)
    }


    override fun convertToDatabaseValue(entityProperty: MedicamentDto?): String? {
        entityProperty ?: return null
        return Gson().toJson(entityProperty)
    }


}