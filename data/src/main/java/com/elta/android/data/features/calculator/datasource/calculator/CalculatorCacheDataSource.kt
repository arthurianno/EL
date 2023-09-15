package com.elta.android.data.features.calculator.datasource.calculator

import com.elta.android.data.features.calculator.cache.model.DishDbEntity
import com.elta.android.data.features.calculator.cache.model.SearchHistoryDbEntity
import com.elta.android.data.features.calculator.mapper.toDb
import com.elta.android.data.features.calculator.mapper.toDomain
import com.elta.android.data.features.common.cache.Cache
import com.elta.android.data.features.common.cache.CommonConditions
import com.elta.android.domain.features.calculator.model.Dish
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import javax.inject.Inject

class CalculatorCacheDataSource @Inject constructor(
    private val searchWordHistory: Cache<SearchHistoryDbEntity>,
    private val dishCache: Cache<DishDbEntity>
) {

    fun getHistoryWords(): Flow<List<String>> = flowOf(
        searchWordHistory.getAll(CommonConditions.All)
            .sortedByDescending { it.time }
            .map { it.word }
    )

    fun saveWordToHistory(word: String) {
        searchWordHistory.update(
            listOf(
                SearchHistoryDbEntity(
                    id = word.hashCode().toLong(),
                    word = word,
                    time = Date().time
                )
            )
        )
    }

    fun cachedDishes(dishes: List<Dish>) = with(dishCache) {
        delete(CommonConditions.All)
        add(dishes.toDb())
    }

    fun getDishesFromCache(): Flow<List<Dish>> = flowOf(
        dishCache.getAll(CommonConditions.All)
            .toDomain()
    )

    fun clearDishesCache() {
        dishCache.delete(CommonConditions.All)
    }
}
