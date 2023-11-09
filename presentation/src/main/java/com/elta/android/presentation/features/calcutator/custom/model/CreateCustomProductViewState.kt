package com.elta.android.presentation.features.calcutator.custom.model

import com.elta.android.domain.features.calculator.model.MetricServingLink
import com.elta.android.presentation.features.calcutator.products.model.DishUiEntity
import com.elta.android.presentation.features.calcutator.products.model.ServingUiEntity

data class CreateCustomProductViewState(
    val isLoading: Boolean,
    val isError: Boolean,
    val dish: DishUiEntity?,
    val product: ProductUiEntity,
    val servings: List<MetricServingLink>?,
    val isShowCountHelpSnack: Boolean
)