package com.elta.android.domain.features.consultant.usecase

import android.net.Uri
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import javax.inject.Inject

class PhotoCreateUseCase @Inject constructor(
    private val repository: ConsultantRepository
) {
    operator fun invoke(): Uri =
        repository.createPhoto()
}
