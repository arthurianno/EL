package com.elta.android.presentation.features.calcutator.model

import androidx.compose.runtime.Immutable

@Immutable
data class CalculatorViewState(
    val dishes: List<DishUiEntity>,
    val startDishes: List<DishUiEntity>,
    val totalBreadUnits: Double,
    val helpText: String,
    val searchInFocus: Boolean,
    val lastWords: List<String>,
    val findingDishes: List<DishUiEntity>,
    val isFindDishes: Boolean
) {
    fun isChanging(): Boolean = dishes != startDishes
}
