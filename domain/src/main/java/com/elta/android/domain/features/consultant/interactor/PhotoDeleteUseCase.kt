package com.elta.android.domain.features.consultant.interactor

import android.net.Uri
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import javax.inject.Inject

class PhotoDeleteUseCase @Inject constructor(
    private val repository: ConsultantRepository
) {
    operator fun invoke(uri: Uri) {
        repository.deletePhoto(uri)
    }
}
