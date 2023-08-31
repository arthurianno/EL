package com.elta.android.data.features.calculator.cache.converter

import com.elta.android.data.features.calculator.cache.model.ServingDbEntity
import com.google.gson.Gson
import io.objectbox.converter.PropertyConverter

class ServingDbEntityConverter : PropertyConverter<ServingDbEntity, String> {
    override fun convertToDatabaseValue(entityProperty: ServingDbEntity): String =
        Gson().toJson(entityProperty)

    override fun convertToEntityProperty(databaseValue: String): ServingDbEntity =
        Gson().fromJson(databaseValue, ServingDbEntity::class.java)
}
