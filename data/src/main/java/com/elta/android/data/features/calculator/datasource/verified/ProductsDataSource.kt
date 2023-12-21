package com.elta.android.data.features.calculator.datasource.verified

import com.elta.android.data.features.calculator.api.ProductApi
import com.elta.android.data.features.calculator.mapper.toDomain
import com.elta.android.data.features.calculator.mapper.toNM
import com.elta.android.data.features.calculator.model.ProductsResponse
import com.elta.android.data.features.user.dto.DiabetesTypeNetworkEntity
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.MetricServingLink
import com.elta.android.domain.features.calculator.model.Product
import com.elta.android.domain.features.user.model.Diabetes
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx2.asFlow
import javax.inject.Inject

class ProductsDataSource @Inject constructor(
    private val api: ProductApi,
) {
    fun getProducts(
        customOnly: Boolean,
        foodName: String?,
        pageIndex: Int,
        pageSize: Int,
        diabetesType: Diabetes?
    ): Single<ProductsResponse> = api.getProducts(
        customOnly = customOnly,
        foodName = foodName,
        diabetesType = diabetesType?.name?.let { DiabetesTypeNetworkEntity.valueOf(it) },
        pageIndex = pageIndex,
        pageSize = pageSize
    )

    fun getProduct(foodId: String): Flow<Dish> =
        api.getProduct(foodId)
            .map { it.toDomain() }
            .asFlow()


    fun getServingsProduct(): Flow<List<MetricServingLink>> =
        api.getServingsProduct().map { servingResponse -> servingResponse.map { it.toDomain() } }
            .asFlow()

    fun removeProduct(productId: String): Flow<String> =
        api.removeProduct(productId).andThen(Observable.just(productId))
            .asFlow()

    fun addProduct(product: Product): Flow<Dish> =
        api.addProduct(product.toNM()).map { it.toDomain() }
            .asFlow()

    fun replaceProduct(product: Product): Flow<Dish> =
        api.replaceProduct(product.toNM()).map { it.toDomain() }
            .asFlow()

}
