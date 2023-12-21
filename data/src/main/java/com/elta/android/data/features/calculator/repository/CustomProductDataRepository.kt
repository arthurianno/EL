package com.elta.android.data.features.calculator.repository

import com.elta.android.data.features.calculator.datasource.verified.ProductsDataSource
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.Product
import com.elta.android.domain.features.calculator.repository.CustomProductRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class CustomProductDataRepository @Inject constructor(
    override val dispatcher: CoroutineDispatcher,
    private val productsDataSource: ProductsDataSource
) : CustomProductRepository {

    override suspend fun deleteCustomProducts(productId: String): Flow<String> {
        return productsDataSource.removeProduct(productId)
            .flowOn(dispatcher)
    }

    override suspend fun addCustomProducts(product: Product): Flow<Dish> {
        return productsDataSource.addProduct(product)
            .flowOn(dispatcher)
    }

    override suspend fun replaceProduct(product: Product): Flow<Dish> {
        return productsDataSource.replaceProduct(product)
            .flowOn(dispatcher)
    }
}
