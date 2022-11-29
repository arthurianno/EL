package com.elta.android.data.features.calculator.api

import android.content.Context
import com.elta.android.data.features.calculator.model.ProductResponse
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
            breadUnits = (it * 3).toDouble()
        )
    }

    override fun getEventProducts(eventId: String) = Observable.just(products)
}
