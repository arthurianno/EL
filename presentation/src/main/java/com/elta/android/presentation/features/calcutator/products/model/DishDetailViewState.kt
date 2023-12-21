package com.elta.android.presentation.features.calcutator.products.model

import androidx.compose.runtime.Immutable
import com.elta.android.domain.features.diary.home.model.CalculatorFlow

@Immutable
data class DishDetailViewState(
    val dish: DishUiEntity,
    val startDish: DishUiEntity,
    val isShowCountHelpSnack: Boolean,
    val calculatorFlow: CalculatorFlow,
    val isLoading: Boolean,
    val isError: Boolean,
)
