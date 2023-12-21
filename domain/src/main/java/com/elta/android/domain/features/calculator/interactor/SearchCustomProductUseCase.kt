package com.elta.android.domain.features.calculator.interactor

import androidx.paging.PagingData
import com.elta.android.domain.features.calculator.model.Dish
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.domain.features.user.model.Diabetes
import com.elta.android.domain.features.user.repository.ProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchCustomProductUseCase @Inject constructor(
    private val repository: CalculatorRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(name: String): Flow<PagingData<Dish>> {
        return repository.getProducts(name = name, onlyCustom = true)
    }
}