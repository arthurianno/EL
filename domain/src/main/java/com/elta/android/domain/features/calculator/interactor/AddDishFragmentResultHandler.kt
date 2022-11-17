package com.elta.android.domain.features.calculator.interactor

import com.elta.android.domain.common.ReturnDataHandler
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddDishFragmentResultHandler @Inject constructor(
    private val repository: CalculatorRepository
) : ReturnDataHandler<Dish> {
    override fun asFlow(): Flow<Dish> =
        repository.addDishFragmentResult.asFlow()

    override fun onNext(data: Dish): Flow<Unit> =
        repository.addDishFragmentResult.onNext(data)
}
