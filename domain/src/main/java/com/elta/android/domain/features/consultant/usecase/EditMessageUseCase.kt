package com.elta.android.domain.features.consultant.usecase

import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import javax.inject.Inject

class EditMessageUseCase @Inject constructor(
    private val repository: ConsultantRepository
) {
    suspend operator fun invoke(messageId: String, newText: String) {
        repository.editMessage(messageId, newText)
    }
}
