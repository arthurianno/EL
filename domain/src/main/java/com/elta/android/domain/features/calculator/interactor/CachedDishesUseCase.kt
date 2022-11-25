package com.elta.android.domain.features.calculator.interactor

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import javax.inject.Inject

class CachedDishesUseCase @Inject constructor(
    private val repository: CalculatorRepository
) {

    suspend operator fun invoke(dishes: List<Dish>) {
        repository.saveLocalDishes(dishes)
    }
}
