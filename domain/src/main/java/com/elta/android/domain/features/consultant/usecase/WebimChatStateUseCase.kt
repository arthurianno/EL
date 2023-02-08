package com.elta.android.domain.features.consultant.usecase

import com.elta.android.domain.features.consultant.model.WebimChatState
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WebimChatStateUseCase @Inject constructor(
    private val repository: ConsultantRepository
) {
    operator fun invoke(): Flow<WebimChatState> =
        repository.chatState()
}
