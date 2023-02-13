package com.elta.android.domain.common.usecase

import com.elta.android.domain.common.repository.MediaRepository
import java.io.File
import javax.inject.Inject

class AudioRecordCreateUseCase @Inject constructor(
    private val repository: MediaRepository
) {
    operator fun invoke(): File = repository.createAudioFile()

}