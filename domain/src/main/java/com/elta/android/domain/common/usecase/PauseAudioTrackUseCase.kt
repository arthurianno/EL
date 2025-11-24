package com.elta.android.domain.common.usecase

import com.elta.android.domain.features.consultant.repository.AudioPlayerRepository
import javax.inject.Inject

class PauseAudioTrackUseCase @Inject constructor(
    private val audioPlayerRepository: AudioPlayerRepository
) {
    operator fun invoke(): Int = audioPlayerRepository.pause()
}
