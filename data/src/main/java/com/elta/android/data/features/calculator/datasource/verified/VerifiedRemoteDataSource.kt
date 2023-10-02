package com.elta.android.data.features.calculator.datasource.verified

import com.elta.android.data.features.calculator.api.CalculatorApi
import com.elta.android.data.features.calculator.mapper.verifiedProductToDish
import com.elta.android.domain.features.calculator.model.Dish
import io.reactivex.Observable
import javax.inject.Inject

class VerifiedRemoteDataSource @Inject constructor(
    private val api: CalculatorApi
) {
    fun getProducts(): Observable<List<Dish>> =
        api.getVerifiedProducts()
            .verifiedProductToDish()
}
