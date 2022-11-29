package com.elta.android.presentation.features.calcutator.model

import androidx.compose.runtime.Immutable
import com.elta.android.domain.features.user.model.Profile

@Immutable
data class CalculatorViewState(
    val profile: Profile,
    val dishes: List<DishUiEntity>,
    val totalBreadUnits: Double,
    val helpText: String,
    val searchInFocus: Boolean,
    val lastWords: List<String>,
    val findingDishes: List<DishUiEntity>,
    val isFindDishes: Boolean
)
