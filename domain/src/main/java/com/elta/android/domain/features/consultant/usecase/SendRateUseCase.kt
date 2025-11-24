package com.elta.android.domain.features.consultant.usecase

import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import javax.inject.Inject

class SendRateUseCase @Inject constructor(
    private val repository: ConsultantRepository
) {
    suspend operator fun invoke(number: Int) {
        repository.sendRate(number)
    }
}
