package com.elta.android.domain.common.usecase

import android.net.Uri
import com.elta.android.domain.common.repository.MediaRepository
import javax.inject.Inject

class PhotoCreateUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(): Uri =
        repository.createPhoto()
}
