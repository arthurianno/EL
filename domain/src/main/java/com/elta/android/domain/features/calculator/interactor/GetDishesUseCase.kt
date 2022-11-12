package com.elta.android.domain.features.calculator.interactor

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDishesUseCase @Inject constructor(
    private val repository: CalculatorRepository
) {
    operator fun invoke(name: String): Flow<List<Dish>> =
        repository.getFoods(name)
}
