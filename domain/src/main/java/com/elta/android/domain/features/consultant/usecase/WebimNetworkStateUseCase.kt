package com.elta.android.domain.features.consultant.usecase

import com.elta.android.domain.features.consultant.model.WebimStatus
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WebimNetworkStateUseCase @Inject constructor(
    private val repository: ConsultantRepository
) {
    operator fun invoke(): Flow<WebimStatus> =
        repository.chatNetworkStatus()
}
