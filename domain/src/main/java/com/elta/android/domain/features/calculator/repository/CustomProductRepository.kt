package com.elta.android.domain.features.calculator.repository

import com.elta.android.common.repository.BaseRepository
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.Product
import kotlinx.coroutines.flow.Flow

interface CustomProductRepository : BaseRepository {
    suspend fun deleteCustomProducts(productId: String): Flow<String>
    suspend fun addCustomProducts(product: Product): Flow<Dish>
    suspend fun replaceProduct(product: Product): Flow<Dish>

}
