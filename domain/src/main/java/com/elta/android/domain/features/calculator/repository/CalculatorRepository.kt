package com.elta.android.domain.features.calculator.repository

import com.elta.android.common.repository.BaseRepository
import com.elta.android.domain.common.ReturnDataHandler
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.DishType
import kotlinx.coroutines.flow.Flow

interface CalculatorRepository : BaseRepository {

    val addDishFragmentResult: ReturnDataHandler<Dish>
    val calculatorFragmentResult: ReturnDataHandler<List<Dish>>

    fun getFatSecretDish(id: String, type: DishType): Flow<Dish>
    fun getFatSecretDishes(name: String): Flow<List<Dish>>
    fun getHistoryList(): Flow<List<String>>
    fun saveWordToHistory(word: String): Flow<Unit>
    fun getEventProducts(eventId: String): Flow<List<Dish>>
}
