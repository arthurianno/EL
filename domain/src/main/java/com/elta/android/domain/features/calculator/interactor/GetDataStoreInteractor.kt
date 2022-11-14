package com.elta.android.domain.features.calculator.interactor

import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDataStoreInteractor @Inject constructor(
    private val repository: CalculatorRepository
) {

    fun dataFlow(): Flow<Dish> =
        repository.fragmentResult

    fun sendData(data: Dish): Flow<Unit> =
        repository.sendFragmentResult(data)
}
