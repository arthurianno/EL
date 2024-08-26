package com.elta.android.data.features.files.cache.model

import com.elta.android.data.features.files.cache.converter.FileTypeConverter
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class FileInfoDbEntity(
    @Id(assignable = true)
    var id: Long,
    val name: String,
    val timestamp: Long,
    @Convert(converter = FileTypeConverter::class, dbType = String::class)
    val type: FileTypeDbEntity
)
