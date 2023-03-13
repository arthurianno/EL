package com.elta.android.domain.common.usecase

import android.net.Uri
import com.elta.android.domain.common.model.FileType
import com.elta.android.domain.common.repository.MediaRepository
import javax.inject.Inject

class CachedSoundUseCase @Inject constructor(
    private val repository: MediaRepository
) {

    suspend operator fun invoke(fileName: String, fileUri: Uri): Uri =
        repository.cachedFile(cacheName = fileName, fileType = FileType.Voice, sourceUri = fileUri)
}