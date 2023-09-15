package com.elta.android.data.features.calculator.cache.converter

import com.elta.android.data.features.calculator.cache.model.ServingDbEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.objectbox.converter.PropertyConverter


class ServingsDbEntityConverter : PropertyConverter<List<ServingDbEntity>, String> {
    override fun convertToDatabaseValue(entityProperty: List<ServingDbEntity>): String =
        Gson().toJson(entityProperty)

    override fun convertToEntityProperty(databaseValue: String): List<ServingDbEntity> {
        val listType = object : TypeToken<List<ServingDbEntity>>() {}.type
        return Gson().fromJson(databaseValue, listType)
    }

}
