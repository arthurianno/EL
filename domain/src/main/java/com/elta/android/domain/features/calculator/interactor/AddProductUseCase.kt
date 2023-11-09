package com.elta.android.domain.features.calculator.interactor

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.Product
import com.elta.android.domain.features.calculator.repository.CustomProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddProductUseCase @Inject constructor(
    private val repository: CustomProductRepository
) {
    suspend operator fun invoke(product: Product): Flow<Dish> {
        return repository.addCustomProducts(product)
    }
}