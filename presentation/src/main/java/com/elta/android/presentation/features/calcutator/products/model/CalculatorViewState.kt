package com.elta.android.presentation.features.calcutator.products.model

import androidx.compose.runtime.Immutable
import com.elta.android.domain.features.diary.home.model.CalculatorFlow

@Immutable
data class CalculatorViewState(
    val dishes: List<DishUiEntity>,
    val startDishes: List<DishUiEntity>,
    val totalBreadUnits: Double?,
    val searchInFocus: Boolean,
    val lastWords: List<String>,
    val calculatorFlow: CalculatorFlow,
    val isLoading: Boolean,
    val isError: Boolean
) {
    fun isChanging(): Boolean = dishes != startDishes
}
