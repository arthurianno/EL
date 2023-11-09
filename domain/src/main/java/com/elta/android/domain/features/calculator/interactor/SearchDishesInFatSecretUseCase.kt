package com.elta.android.domain.features.calculator.interactor

import androidx.paging.PagingData
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchDishesInFatSecretUseCase @Inject constructor( //stopped by customer
    private val repository: CalculatorRepository
) {
    operator fun invoke(name: String): Flow<PagingData<Dish>> {
        return repository.searchDishes(name)
    }
}

