package com.elta.android.presentation.features.calcutator.model

import com.elta.android.domain.features.calculator.model.DishType

data class DishUi(
    val id: String,
    val name: String,
    val type: DishType,
    val portions: List<PortionUi>,
    val isVerification: Boolean,
    val breadUnits: Double
)
