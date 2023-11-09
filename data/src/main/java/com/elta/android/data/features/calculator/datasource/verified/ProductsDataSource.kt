package com.elta.android.data.features.calculator.datasource.verified

import com.elta.android.data.features.calculator.api.ProductApi
import com.elta.android.data.features.calculator.mapper.toDomain
import com.elta.android.data.features.calculator.mapper.toNM
import com.elta.android.data.features.calculator.model.ProductsResponse
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.MetricServingLink
import com.elta.android.domain.features.calculator.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProductsDataSource @Inject constructor(
    private val api: ProductApi
) {
    suspend fun getProducts(
        customOnly: Boolean, foodName: String?, pageIndex: Int, pageSize: Int
    ): ProductsResponse = api.getProducts(
        customOnly = customOnly, foodName = foodName, pageIndex = pageIndex, pageSize = pageSize
    )

    fun getProduct(foodId: String): Flow<Dish> = flow {
        val product = api.getProduct(foodId).toDomain()
        emit(product)
    }


    fun getServingsProduct(): Flow<List<MetricServingLink>> = flow {
        val serving = api.getServingsProduct().map { servingResponse -> servingResponse.toDomain() }
        emit(serving)
    }

    suspend fun removeProduct(productId: String) {
        api.removeProduct(productId)
    }

    suspend fun addProduct(product: Product) = flow {
        val productNM = product.toNM()
        val result = api.addProduct(productNM).toDomain()
        emit(result)
    }

}
