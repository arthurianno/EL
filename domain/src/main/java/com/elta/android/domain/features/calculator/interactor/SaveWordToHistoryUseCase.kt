package com.elta.android.domain.features.calculator.interactor

import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import javax.inject.Inject

class SaveWordToHistoryUseCase @Inject constructor(
    private val repository: CalculatorRepository
) {

    suspend operator fun invoke(word: String) {
        repository.saveWordToHistory(word)
    }
}
