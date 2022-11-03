package com.elta.android.presentation.features.calcutator.model

import com.elta.android.domain.features.user.model.Profile

data class CalculatorState(
    val profile: Profile,
    val dishes: List<DishUi>,
    val helpText: String,
    val searchInFocus: Boolean,
    val lastWords: List<String>,
    val findingDishes: List<DishUi>
)
