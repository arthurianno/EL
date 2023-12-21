package com.elta.android.presentation.features.calcutator.custom.model

import com.elta.android.domain.features.calculator.model.MetricServingLink
import com.elta.android.domain.features.diary.home.model.CalculatorFlow
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity

data class CreateCustomProductViewState(
    val createCustomProductFlow: CreateCustomProductFlow,
    val isLoading: Boolean,
    val isError: Boolean,
    val dish: DishUiEntity?,
    val product: ProductUiEntity,
    val servings: List<MetricServingLink>?,
    val isShowServingCountHelpSnack: Boolean,
    val isShowCarbohydrateCountHelpSnack: Boolean,
    val calculatorFlow: CalculatorFlow
)
