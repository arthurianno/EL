package com.elta.android.data.features.files

import com.elta.android.data.features.files.cache.model.FileInfoDbEntity
import com.elta.android.data.features.files.cache.model.FileTypeDbEntity
import com.elta.android.domain.common.model.FileInfo
import com.elta.android.domain.features.consultant.model.ContentType

fun FileInfoDbEntity.toDomain(): FileInfo =
    FileInfo(
        id = id,
        name = name,
        timestamp = timestamp,
        type = type.toDomain()
    )

fun FileInfo.toDb(): FileInfoDbEntity =
    FileInfoDbEntity(
        id = id,
        name = name,
        timestamp = timestamp,
        type = type.toDb()
    )

private fun FileTypeDbEntity.toDomain(): ContentType =
    when (this) {
        FileTypeDbEntity.Voice -> ContentType.Voice
        FileTypeDbEntity.Image -> ContentType.Image
        FileTypeDbEntity.DocumentPdf -> ContentType.DocumentPdf
        FileTypeDbEntity.Video -> ContentType.Video
    }

private fun ContentType.toDb(): FileTypeDbEntity =
    when (this) {
        ContentType.DocumentPdf -> FileTypeDbEntity.DocumentPdf
        ContentType.Image -> FileTypeDbEntity.Image
        ContentType.Voice -> FileTypeDbEntity.Voice
        ContentType.Video -> FileTypeDbEntity.Video
        else -> FileTypeDbEntity.Voice
    }
