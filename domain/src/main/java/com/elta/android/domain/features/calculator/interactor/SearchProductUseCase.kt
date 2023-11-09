package com.elta.android.domain.features.calculator.interactor

import androidx.paging.PagingData
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchProductUseCase @Inject constructor(
    private val repository: CalculatorRepository
) {
    suspend operator fun invoke(name: String, onlyCustom: Boolean): Flow<PagingData<Dish>> {
        return repository.getProducts(name, onlyCustom)
    }
}