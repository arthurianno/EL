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
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asFlow
import javax.inject.Inject

@OptIn(FlowPreview::class)
class SearchProductUseCase @Inject constructor(
    private val repository: CalculatorRepository,
    private val profileRepository: ProfileRepository,
    private val dispatcher: CoroutineDispatcher
) {
    suspend operator fun invoke(
        name: String,
        calculatorFlow: CalculatorFlow?
    ): Flow<PagingData<Dish>> {
        return profileRepository
            .getProfile()
            .toObservable()
            .asFlow()
            .map { calculatorFlow?.calculatorFlowToDiabetes(it.diabetes) }
            .flowOn(dispatcher)
            .flatMapMerge { diabetes ->
                repository.getProducts(name = name, onlyCustom = false, diabetes = diabetes)
            }
    }

    private fun CalculatorFlow.calculatorFlowToDiabetes(diabetes: Diabetes?): Diabetes? {
        return when {
            this == CalculatorFlow.PRODUCT_ONLY -> Diabetes.SECOND_TABLETS
            this == CalculatorFlow.BREAD_UNITS && diabetes in listOf(
                Diabetes.FIRST,
                Diabetes.SECOND
            ) -> diabetes

            else -> null
        }
    }

}
