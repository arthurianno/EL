package com.elta.android.data.features.files.cache.converter

import com.elta.android.data.features.files.cache.model.FileTypeDbEntity
import io.objectbox.converter.PropertyConverter

class FileTypeConverter : PropertyConverter<FileTypeDbEntity, String> {
    override fun convertToEntityProperty(databaseValue: String?): FileTypeDbEntity? =
        databaseValue?.let { FileTypeDbEntity.valueOf(it) }

    override fun convertToDatabaseValue(entityProperty: FileTypeDbEntity?): String? =
        entityProperty?.name
}
