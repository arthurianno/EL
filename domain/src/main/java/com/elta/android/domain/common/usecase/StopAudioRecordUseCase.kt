package com.elta.android.domain.common.usecase

import com.elta.android.domain.features.consultant.repository.AudioRecorderRepository
import javax.inject.Inject

class StopAudioRecordUseCase @Inject constructor(
    private val audioRepository: AudioRecorderRepository
) {
    operator fun invoke(){
        audioRepository.stop()
    }
}
