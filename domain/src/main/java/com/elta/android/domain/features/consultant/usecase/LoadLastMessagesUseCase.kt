package com.elta.android.domain.features.consultant.usecase

import com.elta.android.domain.features.consultant.model.ConsultantMessage
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import javax.inject.Inject

class LoadLastMessagesUseCase @Inject constructor(
    private val consultantRepository: ConsultantRepository
) {

    suspend operator fun invoke(size: Int): List<ConsultantMessage> =
        consultantRepository.loadLastMessages(size)
}
