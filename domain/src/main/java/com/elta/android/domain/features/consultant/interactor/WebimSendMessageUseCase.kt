package com.elta.android.domain.features.consultant.interactor

import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import javax.inject.Inject

class WebimSendMessageUseCase @Inject constructor(
    private val repository: ConsultantRepository
) {
    suspend operator fun invoke(message: String) {
        repository.sendMessage(message)
    }
}
