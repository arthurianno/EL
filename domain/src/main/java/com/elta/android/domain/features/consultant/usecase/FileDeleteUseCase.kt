package com.elta.android.domain.features.consultant.usecase

import android.net.Uri
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import javax.inject.Inject

class FileDeleteUseCase @Inject constructor(
    private val repository: ConsultantRepository
) {
    suspend operator fun invoke(uri: Uri) {
        repository.deleteFile(uri)
    }
}
