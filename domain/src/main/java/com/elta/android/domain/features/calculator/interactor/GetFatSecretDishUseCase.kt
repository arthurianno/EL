package com.elta.android.domain.features.calculator.interactor

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.DishType
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFatSecretDishUseCase @Inject constructor(
    private val repository: CalculatorRepository
) {
    operator fun invoke(dishId: String, dishType: DishType): Flow<Dish> =
        repository.getFatSecretDish(dishId, dishType)
}
