package com.elta.android.domain.features.calculator.interactor

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import com.elta.android.domain.features.common.FragmentResultInteractor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddDishFragmentResultInteractor @Inject constructor(
    private val repository: CalculatorRepository
) : FragmentResultInteractor<Dish> {

    override fun dataFlow(): Flow<Dish> =
        repository.addDishFragmentResult.fragmentResult

    override fun sendData(data: Dish): Flow<Unit> =
        repository.addDishFragmentResult.sendFragmentResult(data)
}
