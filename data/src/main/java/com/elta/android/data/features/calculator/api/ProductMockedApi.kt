package com.elta.android.data.features.calculator.api

import android.content.Context
import com.elta.android.data.features.calculator.model.ProductItemResponse
import com.elta.android.data.features.calculator.model.ProductsResponse
import com.elta.android.data.features.calculator.model.MetricServingUnitResponse
import com.elta.android.data.features.calculator.model.ServingResponse
import com.elta.android.data.features.calculator.model.StoredProductNetworkEntity
import com.elta.android.data.features.common.dto.MetaDto
import kotlinx.coroutines.delay
import timber.log.Timber
import kotlin.random.Random

class ProductMockedApi(context: Context) : ProductApi {

    override suspend fun addProduct(storedProduct: StoredProductNetworkEntity): ProductItemResponse {
        Timber.d("MOCK: product saved")
        val servings = storedProduct.servings.map {
            ServingResponse(
                servingId = it.servingId,
                carbohydrate = it.carbohydrate,
                metricServingUnit = it.metricServingUnit,
                metricServingAmount = it.metricServingAmount,
                calories = it.calories,
                fat = it.fat,
                protein = it.protein
            )
        }
        return ProductItemResponse(
            isVerified = false,
            foodId = storedProduct.foodId,
            foodName = storedProduct.foodName,
            servings = servings
        )
    }

    override suspend fun getProducts(
        customOnly: Boolean,
        foodName: String?,
        pageIndex: Int,
        pageSize: Int
    ): ProductsResponse {
        delay(1000L)

        val items = generateProduct(customOnly, foodName, pageIndex, pageSize)
        return ProductsResponse(
            items = items,
            meta = MetaDto(
                totalItems = 100,
                currentPage = pageIndex,
                pageSize = pageSize
            )
        )
    }

    override suspend fun getProduct(foodId: String): ProductItemResponse {
        delay(1000L)
        return generateProduct(false, "", 1, 1).first()
    }

    override suspend fun removeProduct(foodId: String) {
        Timber.d("MOCK: product removed")
    }

    override suspend fun getServingsProduct(): List<MetricServingUnitResponse> {
        delay(1000L)
        val list = mutableListOf<MetricServingUnitResponse>()
        repeat(3) {
            list.add(generateMetricServingUnit(it))
        }
        return list
    }

    private fun generateProduct(
        customOnly: Boolean,
        foodName: String?,
        pageIndex: Int,
        pageSize: Int
    ): List<ProductItemResponse> {
        val items = mutableListOf<ProductItemResponse>()
        repeat(pageSize) { id ->
            items.add(
                ProductItemResponse(
                    isVerified = if (customOnly) false else Random.nextBoolean(),
                    foodId = "$foodName $pageIndex-$id",
                    foodName = "Банан #$pageIndex #$id",
                    servings = generateServings(3)
                )
            )
        }
        return items
    }

    private fun generateServings(count: Int): List<ServingResponse> {
        val mutableList = mutableListOf<ServingResponse>()
        repeat(count) {
            mutableList.add(
                ServingResponse(
                    servingId = "$count + serving",
                    carbohydrate = count,
                    metricServingAmount = 1.0,
                    metricServingUnit = generateMetricServingUnit(it),
                    calories = null,
                    fat = null,
                    protein = null,
                )

            )
        }
        return mutableList
    }

    private fun generateMetricServingUnit(it: Int) = MetricServingUnitResponse(
        id = it,
        name = "шт $it",
    )
}
