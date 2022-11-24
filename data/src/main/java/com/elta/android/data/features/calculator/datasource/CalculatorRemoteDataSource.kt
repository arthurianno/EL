package com.elta.android.data.features.calculator.datasource

import com.elta.android.data.features.calculator.api.CalculatorApi
import com.elta.android.data.features.calculator.mapper.toDomain
import com.elta.android.domain.features.calculator.model.Dish
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asFlow
import javax.inject.Inject

class CalculatorRemoteDataSource @Inject constructor(
    private val api: CalculatorApi
) {

    fun getProducts(evenId: String): Flow<List<Dish>> =
        api.getEventProducts(evenId)
            .asFlow()
            .map { it.toDomain() }
}
