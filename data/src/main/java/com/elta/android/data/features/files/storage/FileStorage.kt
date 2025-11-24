package com.elta.android.data.features.files.storage

import android.graphics.Bitmap
import android.net.Uri
import com.elta.android.domain.common.model.FileDirectory
import java.io.File

interface FileStorage {
    fun getImageFile(fileName: String, directoryName: String): File
    fun saveBitmap(fileName: String, directoryName: String, bitmap: Bitmap): File
    fun getFileUri(file: File): Uri
    fun createCachedPhoto(
        fileName: String,
        imageQuality: Int = FINE_IMAGE_QUALITY,
        bitmap: Bitmap? = null
    ): Uri

    fun createCachedAudio(fileName: String): File
    suspend fun createCompressedCachedFile(fileName: String, sourceUri: Uri, maxSize: Int): Uri?
    fun getCacheFile(fileName: String?): File?
    fun getFileFromDirectory(directoryFile: File, fileName: String): File?
    fun getAllCache(): List<File>
    fun getAllFromDirectory(): List<File>
    fun deleteFile(uri: Uri)
    fun cacheFileInDirectory(filePath: String, sourceUri: Uri): Uri
    fun getDirectory(dir: FileDirectory): String
    fun getFileType(uri: Uri): String?
    fun createCacheFile(fileName: String): File

}