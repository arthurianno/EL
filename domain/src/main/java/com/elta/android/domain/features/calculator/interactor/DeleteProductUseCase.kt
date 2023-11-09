package com.elta.android.domain.features.calculator.interactor

import com.elta.android.domain.features.calculator.repository.CustomProductRepository
import javax.inject.Inject

class DeleteProductUseCase @Inject constructor(
    private val repository: CustomProductRepository
) {
    suspend operator fun invoke(id: String) {
        return repository.deleteCustomProducts(id)
    }
}