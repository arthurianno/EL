package com.elta.android.domain.features.consultant.usecase

import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import timber.log.Timber
import javax.inject.Inject

class DeleteMessageUseCase @Inject constructor(
    private val repository: ConsultantRepository
) {
    suspend operator fun invoke(id: String) {
        try {
            repository.deleteMessage(id)
        } catch (e: Throwable) {
            Timber.e("This message can't be deleted")
        }
    }
}
