package com.elta.android.domain.features.calculator.interactor

import com.elta.android.domain.features.calculator.model.MetricServingLink
import com.elta.android.domain.features.calculator.model.Serving
import com.elta.android.domain.features.calculator.repository.CalculatorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetServingsProductUseCase @Inject constructor(
    private val repository: CalculatorRepository
) {
    operator fun invoke(): Flow<List<MetricServingLink>> {
        return repository.getServingsProduct()
    }
}