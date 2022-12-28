package com.elta.android.domain.features.consultant.interactor

import com.elta.android.domain.features.consultant.model.ChatList
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WebimGetMessagesUseCase @Inject constructor(
    private val repository: ConsultantRepository
) {
    operator fun invoke(): Flow<ChatList> =
        repository.chat
}
