package com.elta.android.domain.features.calculator.repository

import com.elta.android.common.repository.BaseRepository
import com.elta.android.domain.features.calculator.model.Dish
import kotlinx.coroutines.flow.Flow

interface CalculatorRepository : BaseRepository {

    fun getFood(id: String): Flow<Dish>

    fun getFoods(name: String): Flow<List<Dish>>
}
