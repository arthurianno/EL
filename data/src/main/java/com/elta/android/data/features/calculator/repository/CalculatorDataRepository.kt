package com.elta.android.data.features.calculator.repository

import com.elta.android.data.features.calculator.datasource.FatSecretDataSource
import com.elta.android.data.features.calculator.storage.FatSecretDataStorage
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.Date
import javax.inject.Inject

class CalculatorDataRepository @Inject constructor(
    private val fatSecretDataSource: FatSecretDataSource,
    private val storage: FatSecretDataStorage,
    override val dispatcher: CoroutineDispatcher
) : CalculatorRepository {

    // TODO("Тестовая реализация хранения списка История Поиска. Убрать после полноценной реализации")
    private val historyWords: MutableMap<String, Long> = mutableMapOf()
    override fun getFood(dish: Dish): Flow<Dish> =
        fatSecretDataSource.getFood(dish)
            .flowOn(dispatcher)

    override fun getFoods(name: String): Flow<List<Dish>> =
        fatSecretDataSource.getFoods(name)
            .flowOn(dispatcher)

    override fun getHistoryList(): Flow<List<String>> = flow {
        emit(
            historyWords
                .toList()
                .sortedByDescending { it.second }
                .map { it.first }
        )
    }

    override fun saveWordToHistory(word: String): Flow<Unit> = flow {
        historyWords[word] = Date().time
        emit(Unit)
    }
}
