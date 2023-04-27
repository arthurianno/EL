package com.elta.android.domain.common.usecase

import android.net.Uri
import com.elta.android.domain.common.repository.MediaRepository
import javax.inject.Inject

class FileDeleteUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    suspend operator fun invoke(uri: Uri) {
        repository.deleteFile(uri)
    }
}
