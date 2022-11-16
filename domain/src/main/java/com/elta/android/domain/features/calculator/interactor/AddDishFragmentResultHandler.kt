package com.elta.android.domain.features.calculator.interactor

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import com.elta.android.domain.features.common.ReturnDataHandler
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddDishFragmentResultHandler @Inject constructor(
    private val repository: CalculatorRepository
) : ReturnDataHandler<Dish> {
    override fun asFlow(): Flow<Dish> =
        repository.addDishFragmentResult.asFlow()

    override fun returnResult(data: Dish): Flow<Unit> =
        repository.addDishFragmentResult.returnResult(data)
}
