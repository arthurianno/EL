package com.elta.android.domain.features.calculator.interactor

import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveWordToHistoryUseCase @Inject constructor(
    private val repository: CalculatorRepository
) {

    operator fun invoke(word: String): Flow<Unit> =
        repository.saveWordToHistory(word)
}
