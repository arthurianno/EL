package com.elta.android.data.features.calculator.api

import android.content.Context
import com.elta.android.data.features.calculator.model.ProductItemResponse
import com.elta.android.data.features.calculator.model.ProductsResponse
import com.elta.android.data.features.calculator.model.MetricServingUnitResponse
import com.elta.android.data.features.calculator.model.ServingResponse
import com.elta.android.data.features.calculator.model.StoredProductNetworkEntity
import com.elta.android.data.features.common.dto.MetaDto
import com.elta.android.data.features.user.dto.DiabetesTypeNetworkEntity
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import kotlin.random.Random

class ProductMockedApi(context: Context) : ProductApi {

    override fun addProduct(storedProduct: StoredProductNetworkEntity): Observable<ProductItemResponse> {
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
        return Observable.fromCallable {
            ProductItemResponse(
                isVerified = false,
                foodId = storedProduct.foodId,
                foodName = storedProduct.foodName,
                servings = servings
            )
        }
    }

    override fun replaceProduct(storedProduct: StoredProductNetworkEntity): Observable<ProductItemResponse> {
        return addProduct(storedProduct)
    }

    override fun getProducts(
        diabetesType: DiabetesTypeNetworkEntity?,
        customOnly: Boolean,
        foodName: String?,
        pageIndex: Int,
        pageSize: Int
    ): Single<ProductsResponse> {

        val items = generateProduct(customOnly, foodName, pageIndex, pageSize)
        return Single.fromCallable {
            ProductsResponse(
                items = items,
                meta = MetaDto(
                    totalItems = 100,
                    currentPage = pageIndex,
                    pageSize = pageSize
                )
            )
        }
    }

    override fun getProduct(foodId: String): Observable<ProductItemResponse> {
        return Observable.fromCallable { generateProduct(false, "", 1, 1).first() }
    }

    override fun removeProduct(foodId: String): Completable {
        return Completable.fromCallable {
            Timber.d("MOCK: product removed")
        }
    }

    override fun getServingsProduct(): Observable<List<MetricServingUnitResponse>> {
        val list = mutableListOf<MetricServingUnitResponse>()
        repeat(3) {
            list.add(generateMetricServingUnit(it))
        }
        return Observable.fromCallable { list }
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
