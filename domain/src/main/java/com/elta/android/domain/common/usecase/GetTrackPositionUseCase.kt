package com.elta.android.domain.common.usecase

import com.elta.android.domain.features.consultant.repository.AudioPlayerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrackPositionUseCase @Inject constructor(
    private val audioPlayerRepository: AudioPlayerRepository
) {
    operator fun invoke(): Flow<Int> = audioPlayerRepository.trackPosition
}
