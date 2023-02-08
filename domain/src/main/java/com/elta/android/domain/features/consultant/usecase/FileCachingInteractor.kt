package com.elta.android.domain.features.consultant.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.elta.android.domain.common.model.FileType
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import javax.inject.Inject

class FileCachingInteractor @Inject constructor(
    private val repository: ConsultantRepository,
) {

    suspend fun savePhoto(name: String, bitmap: Bitmap) {
        repository.cachedPhoto(name, bitmap)
    }

    suspend fun savePgf(name: String, sourceUri: Uri): Uri =
        repository.cachedFile(cacheName = name, fileType = FileType.Pdf, sourceUri = sourceUri)

    suspend fun saveJpg(name: String, sourceUri: Uri): Uri =
        repository.cachedFile(cacheName = name, fileType = FileType.Jpg, sourceUri = sourceUri)

    suspend fun clearCache() {
        repository.clearCache()
    }
}
