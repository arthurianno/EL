package com.elta.android.data.features.calculator.repository

import com.elta.android.data.features.calculator.datasource.FatSecretDataSource
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class CalculatorDataRepository @Inject constructor(
    private val fatSecretDataSource: FatSecretDataSource,
    override val dispatcher: CoroutineDispatcher
) : CalculatorRepository {
    override fun getFood(id: String): Flow<Dish> =
        fatSecretDataSource.getFood(id)
            .flowOn(dispatcher)

    override fun getFoods(name: String): Flow<List<Dish>> =
        fatSecretDataSource.getFoods(name)
            .flowOn(dispatcher)
}
