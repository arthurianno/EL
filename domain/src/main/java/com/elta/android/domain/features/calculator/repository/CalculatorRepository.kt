package com.elta.android.domain.features.calculator.repository

import androidx.paging.PagingData
import com.elta.android.common.repository.BaseRepository
import com.elta.android.domain.common.ReturnDataHandler
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.model.DishType
import kotlinx.coroutines.flow.Flow

interface CalculatorRepository : BaseRepository {

    val addDishFragmentResult: ReturnDataHandler<Dish>
    val calculatorFragmentResult: ReturnDataHandler<List<Dish>>

    fun getFatSecretDish(id: String, type: DishType): Flow<Dish>
    fun searchDishes(name: String): Flow<PagingData<Dish>>
    fun getHistoryList(): Flow<List<String>>
    suspend fun saveWordToHistory(word: String)
    fun getEventProducts(eventId: String): Flow<List<Dish>>
    fun getLocalDishes(): Flow<List<Dish>>
    suspend fun saveLocalDishes(dishes: List<Dish>)
    suspend fun clearLocalDishes()
}
