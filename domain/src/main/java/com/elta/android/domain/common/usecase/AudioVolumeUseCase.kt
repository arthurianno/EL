package com.elta.android.domain.common.usecase

import com.elta.android.domain.features.consultant.repository.AudioRecorderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AudioVolumeUseCase @Inject constructor(
    private val audioRepository: AudioRecorderRepository
) {
    operator fun invoke(): Flow<Float> =
        audioRepository.volume
            .map { volume ->
                when {
                    volume < 0.1 -> 0.1f
                    volume > 1 -> 1f
                    else -> volume
                }
            }
}
