package com.elta.android.domain.common.usecase

import android.graphics.Bitmap
import com.elta.android.domain.common.repository.MediaRepository
import javax.inject.Inject

class CachedBitmapUseCase @Inject constructor(
    private val repository: MediaRepository
) {

    suspend operator fun invoke(fileName: String, bitmap: Bitmap) {
        repository.cachedPhoto(name = fileName, bitmap = bitmap)
    }
}
