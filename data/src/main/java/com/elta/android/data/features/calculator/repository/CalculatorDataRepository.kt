package com.elta.android.data.features.calculator.repository

import com.elta.android.common.utils.takeFirst
import com.elta.android.data.features.calculator.datasource.CalculatorCacheDataSource
import com.elta.android.data.features.calculator.datasource.CalculatorRemoteDataSource
import com.elta.android.data.features.calculator.datasource.FatSecretDataSource
import com.elta.android.domain.common.ReturnDataHandler
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.DishType
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val HISTORY_LIST_LENGTH = 5

class CalculatorDataRepository @Inject constructor(
    private val fatSecretDataSource: FatSecretDataSource,
    private val remote: CalculatorRemoteDataSource,
    private val cache: CalculatorCacheDataSource,
    override val dispatcher: CoroutineDispatcher
) : CalculatorRepository {

    override val addDishFragmentResult = ReturnDataHandler.resultObject<Dish>()
    override val calculatorFragmentResult = ReturnDataHandler.resultObject<List<Dish>>()

    override fun getFatSecretDish(id: String, type: DishType): Flow<Dish> =
        fatSecretDataSource.getFood(id, type)
            .flowOn(dispatcher)

    override fun getFatSecretDishes(name: String): Flow<List<Dish>> =
        fatSecretDataSource.getFoods(name)
            .flowOn(dispatcher)

    override fun getHistoryList(): Flow<List<String>> =
        cache.getHistoryWords()
            .map { it.takeFirst(HISTORY_LIST_LENGTH) }
            .flowOn(dispatcher)

    override suspend fun saveWordToHistory(word: String) {
        cache.saveWordToHistory(word)
    }

    override fun getEventProducts(eventId: String): Flow<List<Dish>> =
        remote.getProducts(eventId)
            .flowOn(dispatcher)

    override fun getLocalDishes(): Flow<List<Dish>> =
        cache.getDishesFromCache()
            .flowOn(dispatcher)

    override suspend fun saveLocalDishes(dishes: List<Dish>) {
        cache.cachedDishes(dishes)
    }

    override suspend fun clearLocalDishes() {
        cache.clearDishesCache()
    }
}
