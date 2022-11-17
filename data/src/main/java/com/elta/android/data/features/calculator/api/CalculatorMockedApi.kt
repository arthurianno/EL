package com.elta.android.data.features.calculator.api

import android.content.Context
import com.elta.android.data.features.calculator.dto.ProductDto
import io.reactivex.Observable
import java.util.Random
import java.util.UUID

class CalculatorMockedApi(context: Context) : CalculatorApi {

    private val products = (1..Random(20).nextInt().plus(3)).map {
        ProductDto(
            id = UUID.randomUUID().toString(),
            name = "Продукт $it",
            servingAmount = Random().nextInt(it).toDouble(),
            servingId = UUID.randomUUID().toString(),
            servingName = "штук",
            breadUnits = (it * 3).toDouble()
        )
    }

    override fun getEventProducts(eventId: String) = Observable.just(products)
}
