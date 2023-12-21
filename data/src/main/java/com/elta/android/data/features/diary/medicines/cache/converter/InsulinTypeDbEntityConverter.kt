package com.elta.android.data.features.diary.medicines.cache.converter

import com.elta.android.data.features.diary.medicines.cache.entity.InsulinTypeDbEntity
import com.google.gson.Gson
import io.objectbox.converter.PropertyConverter

class InsulinTypeDbEntityConverter : PropertyConverter<InsulinTypeDbEntity, String> {

    override fun convertToEntityProperty(databaseValue: String?): InsulinTypeDbEntity? {
        databaseValue ?: return null
        return Gson().fromJson(databaseValue, InsulinTypeDbEntity::class.java)
    }

    override fun convertToDatabaseValue(entityProperty: InsulinTypeDbEntity?): String? {
        entityProperty ?: return null
        return Gson().toJson(entityProperty)
    }


}