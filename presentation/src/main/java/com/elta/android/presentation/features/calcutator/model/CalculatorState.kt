package com.elta.android.presentation.features.calcutator.model

import androidx.compose.runtime.Immutable
import com.elta.android.domain.features.user.model.Profile

@Immutable
data class CalculatorState(
    val profile: Profile,
    val dishes: List<DishUi>,
    val helpText: String,
    val searchInFocus: Boolean,
    val lastWords: List<String>,
    val findingDishes: List<DishUi>,
    val isFindDishes: Boolean
)
