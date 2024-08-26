package com.elta.android.domain.features.consultant.usecase

import com.elta.android.domain.common.repository.MediaRepository
import javax.inject.Inject

class GetAudioDurationUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(fileName: String) = repository.getDuration(fileName)
}
