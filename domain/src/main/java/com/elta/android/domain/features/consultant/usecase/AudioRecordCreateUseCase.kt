package com.elta.android.domain.features.consultant.usecase

import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import java.io.File
import javax.inject.Inject

class AudioRecordCreateUseCase @Inject constructor(
    private val repository: ConsultantRepository
) {
    operator fun invoke(): File = repository.createAudioFile()

}