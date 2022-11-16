package com.elta.android.domain.features.calculator.interactor

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import com.elta.android.domain.features.common.FragmentResultInteractor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CalculatorFragmentResultInteractor @Inject constructor(
    private val repository: CalculatorRepository
) : FragmentResultInteractor<List<Dish>> {

    override fun dataFlow(): Flow<List<Dish>> =
        repository.calculatorFragmentResult.fragmentResult

    override fun sendData(data: List<Dish>): Flow<Unit> =
        repository.calculatorFragmentResult.sendFragmentResult(data)
}
