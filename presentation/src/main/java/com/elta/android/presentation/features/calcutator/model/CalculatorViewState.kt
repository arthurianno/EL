package com.elta.android.presentation.features.calcutator.model

import androidx.compose.runtime.Immutable
import androidx.paging.PagingData

@Immutable
data class CalculatorViewState(
    val dishes: List<DishUiEntity>,
    val startDishes: List<DishUiEntity>,
    val totalBreadUnits: Double,
    val helpText: String,
    val searchInFocus: Boolean,
    val lastWords: List<String>,
    val isLoading: Boolean,
    val isError: Boolean
) {
    fun isChanging(): Boolean = dishes != startDishes
}
