package com.elta.android.domain.common.repository

import android.graphics.Bitmap
import android.net.Uri
import com.elta.android.domain.common.model.FileType
import java.io.File

interface MediaRepository {
    fun createAudioFile(): File
    fun createPhoto(): Uri
    suspend fun cachedPhoto(name: String, bitmap: Bitmap): Uri
    suspend fun cachedFile(cacheName: String, fileType: FileType? = null, sourceUri: Uri): Uri
    suspend fun deleteFile(uri: Uri)
    suspend fun clearCache()
}