package com.elta.android.presentation.features.calcutator.model

data class DishUi(
    val id: String,
    val name: String,
    val portions: List<PortionUi>,
    val isVerification: Boolean,
    val breadUnits: Double
)
