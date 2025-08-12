package com.elta.android.domain.common.repository

import android.graphics.Bitmap
import android.net.Uri
import com.elta.android.domain.common.model.FileDirectory
import com.elta.android.domain.common.model.FileType
import java.io.File

interface MediaRepository {
    fun createAudioFile(): File
    fun createPhoto(): Uri
    suspend fun cachedPhoto(name: String, bitmap: Bitmap): Uri
    suspend fun cachedFile(fileName: String, fileType: FileType, sourceUri: Uri): Uri?
    fun deleteFile(uri: Uri)
    fun getFileUri(file: File): Uri
    suspend fun clearCache()
    fun cacheFileInDirectory(directory: FileDirectory, fileName: String, uri: Uri): Uri
    fun getFileFromDirectory(directory: FileDirectory, fileName: String): File?
    fun getCachedFile(fileName: String): File?
    fun getFileType(uri: Uri): FileType?
    fun getFileSize(uri: Uri): Long?
    fun getDuration(fileName: String): Int
    suspend fun saveImageToCache(data: ByteArray, fileName: String): Uri?
    suspend fun getBitmapFromUri(uri: Uri?): Bitmap?

}
