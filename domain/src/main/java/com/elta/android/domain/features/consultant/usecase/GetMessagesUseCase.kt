package com.elta.android.domain.features.consultant.usecase

import com.elta.android.domain.features.consultant.model.ConsultantChat
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val repository: ConsultantRepository
) {
    operator fun invoke(): Flow<ConsultantChat> =
        repository.chat
}
