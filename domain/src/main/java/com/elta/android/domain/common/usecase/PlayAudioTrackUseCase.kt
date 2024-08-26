package com.elta.android.domain.common.usecase

import android.net.Uri
import com.elta.android.domain.features.consultant.repository.AudioPlayerRepository
import javax.inject.Inject

class PlayAudioTrackUseCase @Inject constructor(
    private val audioPlayerRepository: AudioPlayerRepository
) {
    operator fun invoke(uri: Uri, trackPosition: Int? = null) {
        audioPlayerRepository.start(uri, trackPosition)
    }
}
