package com.elta.android.domain.features.emias.interactor

import com.elta.android.domain.features.emias.repository.EmiasRepository
import javax.inject.Inject

class UnbindEmiasUseCase @Inject constructor(
    private val emiasRepository: EmiasRepository
) {

    suspend operator fun invoke() {
        emiasRepository.unbindProfile()
    }
}
