package com.elta.android.data.features.consultant.repository

import android.graphics.Bitmap
import android.net.Uri
import com.elta.android.data.features.common.storage.FileStorage
import com.elta.android.domain.common.addExtension
import com.elta.android.domain.common.model.FileType
import com.elta.android.domain.common.repository.MediaRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val PHOTO_NAME_PREFIX = "eltaPhoto_"
private const val AUDIO_NAME_PREFIX = "eltaAudio_"
private const val NAME_DATE_STAMP_PATTERN = "yyyyMMdd_HHmmss"

class MediaDataRepository @Inject constructor(
    private val fileStorage: FileStorage
) : MediaRepository {
    override fun createAudioFile() =
        fileStorage.createCachedAudio(
            AUDIO_NAME_PREFIX + SimpleDateFormat(
                NAME_DATE_STAMP_PATTERN,
                Locale.getDefault()
            ).format(Date())
        )

    override fun createPhoto(): Uri =
        fileStorage.createCachedPhoto(
            PHOTO_NAME_PREFIX + SimpleDateFormat(
                NAME_DATE_STAMP_PATTERN,
                Locale.getDefault()
            ).format(Date())
        )

    override suspend fun cachedPhoto(name: String, bitmap: Bitmap) =
        fileStorage.createCachedPhoto(fileName = name, bitmap = bitmap)

    override suspend fun cachedFile(cacheName: String, fileType: FileType, sourceUri: Uri): Uri =
        fileStorage.createCachedFile(
            cachedFileName = cacheName addExtension fileType,
            sourceUri = sourceUri
        )


    override suspend fun deleteFile(uri: Uri) {
        fileStorage.deleteFile(uri)
    }

    override suspend fun clearCache() {
        fileStorage.getAllCache()
            .forEach {
                fileStorage.deleteFile(fileStorage.getFileUri(it))
            }
    }
}