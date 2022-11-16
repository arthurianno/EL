package com.elta.android.domain.features.calculator.interactor

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import com.elta.android.domain.features.common.ReturnDataHandler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CalculatorFragmentResultHandler @Inject constructor(
    private val repository: CalculatorRepository
) : ReturnDataHandler<List<Dish>> {
    override fun asFlow(): Flow<List<Dish>> =
        repository.calculatorFragmentResult.asFlow()

    override fun returnResult(data: List<Dish>): Flow<Unit> =
        repository.calculatorFragmentResult.returnResult(data)
}
