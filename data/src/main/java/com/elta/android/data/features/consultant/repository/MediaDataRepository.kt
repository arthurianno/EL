package com.elta.android.data.features.consultant.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.elta.android.data.features.files.metadata.FileMetadataReader
import com.elta.android.data.features.files.storage.FileStorage
import com.elta.android.domain.common.addExtension
import com.elta.android.domain.common.getFileExtension
import com.elta.android.domain.common.model.FileDirectory
import com.elta.android.domain.common.model.FileType
import com.elta.android.domain.common.model.FileType.Companion.getByExtension
import com.elta.android.domain.common.model.FileType.Companion.toFileType
import com.elta.android.domain.common.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private const val PHOTO_NAME_PREFIX = "eltaPhoto_"
private const val AUDIO_NAME_PREFIX = "eltaAudio_"
private const val NAME_DATE_STAMP_PATTERN = "yyyyMMdd_HHmmss"
private const val MAX_IMAGE_SIZE = 5 * 1024 * 1024

class MediaDataRepository @Inject constructor(
    private val fileStorage: FileStorage,
    private val fileMetadataReader: FileMetadataReader,
    private val context: Context
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

    override suspend fun cachedFile(fileName: String, fileType: FileType, sourceUri: Uri): Uri? =
        fileStorage.createCompressedCachedFile(
            fileName = fileName addExtension fileType,
            sourceUri = sourceUri,
            maxSize = MAX_IMAGE_SIZE
        )

    override fun deleteFile(uri: Uri) {
        fileStorage.deleteFile(uri)
    }

    override fun getFileUri(file: File): Uri =
        fileStorage.getFileUri(file)

    override suspend fun clearCache() {
        fileStorage.getAllCache()
            .forEach {
                fileStorage.deleteFile(fileStorage.getFileUri(it))
            }
    }

    override fun cacheFileInDirectory(directory: FileDirectory, fileName: String, uri: Uri): Uri {
        val directoryPath = fileStorage.getDirectory(directory)

        return fileStorage.cacheFileInDirectory(
            filePath = directoryPath + File.separator + fileName,
            sourceUri = uri
        )
    }

    override fun getFileFromDirectory(directory: FileDirectory, fileName: String): File? {
        val directoryPath = fileStorage.getDirectory(directory)
        val folder = File(directoryPath)

        return fileStorage.getFileFromDirectory(folder, fileName)
    }

    override fun getCachedFile(fileName: String): File? =
        fileStorage.getCacheFile(fileName)

    override fun getFileType(uri: Uri): FileType? {
        val type = fileStorage.getFileType(uri).toFileType()

        val extension = uri.getFileExtension()
        val typeByExtension = extension?.let { getByExtension(it) }

        return type ?: typeByExtension
    }

    override fun getDuration(fileName: String): Int {
        val file = getCachedFile(fileName)
        return file?.let { fileMetadataReader.getMediaDuration(it.absolutePath) } ?: 0
    }

    override fun getFileSize(uri: Uri): Long? {
        return try {
            val file = File(uri.path ?: return null)
            if (file.exists()) {
                file.length() // Возвращаем размер файла в байтах
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    override suspend fun saveImageToCache(data: ByteArray, fileName: String): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val file = fileStorage.createCacheFile(fileName)
                file.writeBytes(data)
                fileStorage.getFileUri(file)
            } catch (e: Exception) {
                Log.e("MediaDataRepository", "Failed to save image to cache: ${e.message}")
                null
            }
        }
    }

    override suspend fun getBitmapFromUri(uri: Uri?): Bitmap? {
        if (uri == null) {
            Log.e("MediaRepository", "URI is null")
            return null
        }
        return withContext(Dispatchers.IO) {
            try {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it, null, this)
                    }
                    inJustDecodeBounds = false
                    inSampleSize = calculateInSampleSize(outWidth, outHeight, 512, 512)
                }
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it, null, options)
                }
            } catch (e: Exception) {
                Log.e("MediaRepository", "Failed to decode bitmap from URI $uri: ${e.message}")
                null
            }
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, reqWidth: Int, reqHeight: Int): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

}
