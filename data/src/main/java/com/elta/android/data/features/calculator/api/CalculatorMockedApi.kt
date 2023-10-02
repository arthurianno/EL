package com.elta.android.data.features.calculator.api

import android.content.Context
import com.elta.android.data.features.calculator.model.ProductResponse
import com.elta.android.data.features.calculator.model.VerifiedProductResponse
import com.elta.android.domain.features.calculator.model.DishType
import io.reactivex.Observable
import java.util.Random
import java.util.UUID

class CalculatorMockedApi(context: Context) : CalculatorApi {

    private val products = (1..Random(20).nextInt().plus(3)).map {
        ProductResponse(
            id = UUID.randomUUID().toString(),
            name = "Продукт $it",
            type = DishType.values()[Random().nextInt()].name,
            servingAmount = Random().nextInt(it).toDouble(),
            servingId = UUID.randomUUID().toString(),
            servingName = "штук",
            breadUnits = (it * 3).toDouble(),
            brandName = "Производитель",
            calories = 100.0,
            proteins = 15.0,
            fats = 30.0,
            carbohydrates = 45.0,
            isVerified = false
        )
    }

    override fun getEventProducts(eventId: String) = Observable.just(products)

    override fun getVerifiedProducts(): Observable<List<VerifiedProductResponse>> {
        return Observable.fromCallable {
            listOf(
                VerifiedProductResponse(
                    isVerified = true,
                    foodId = "12dw-qwdqw-fqe-wf-wgewr-3r2",
                    foodName = "Банан",
                    servings = listOf(
                        VerifiedProductResponse.Serving(
                            servingId = "12d0-qwd-f1f0-saf",
                            carbohydrate = 12.0,
                            fat = null,
                            calories = null,
                            protein = null,
                            metricServingAmount = 100.0,
                            metricServingUnit = "гр"
                        )
                    )
                ),
                VerifiedProductResponse(
                    isVerified = true,
                    foodId = "12dw-qwdqw-fqe-wf-wgewr-3r2",
                    foodName = "Банан спелый и умелый",
                    servings = listOf(
                        VerifiedProductResponse.Serving(
                            servingId = "1-w-d1-d-afsafq1-1f",
                            carbohydrate = 12.0,
                            fat = null,
                            calories = null,
                            protein = null,
                            metricServingAmount = 2.0,
                            metricServingUnit = "штуки по 100-150 гр"
                        )
                    )
                )
            )
        }
    }
}
