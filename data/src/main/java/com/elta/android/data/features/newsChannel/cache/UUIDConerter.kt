package com.elta.android.data.features.newsChannel.cache


import io.objectbox.converter.PropertyConverter
import java.util.UUID

class UUIDConverter : PropertyConverter<UUID, String> {
    override fun convertToEntityProperty(databaseValue: String?): UUID? {
        return databaseValue?.let { UUID.fromString(it) }
    }

    override fun convertToDatabaseValue(entityProperty: UUID?): String? {
        return entityProperty?.toString()
    }
}