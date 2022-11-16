package com.elta.android.domain.features.calculator.repository

import com.elta.android.common.repository.BaseRepository
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.common.ReturnDataHandler
import kotlinx.coroutines.flow.Flow

interface CalculatorRepository : BaseRepository {

    val addDishFragmentResult: ReturnDataHandler<Dish>
    val calculatorFragmentResult: ReturnDataHandler<List<Dish>>

    fun getFood(dish: Dish): Flow<Dish>
    fun getFoods(name: String): Flow<List<Dish>>
    fun getHistoryList(): Flow<List<String>>
    fun saveWordToHistory(word: String): Flow<Unit>
}
