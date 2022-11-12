package com.elta.android.domain.features.calculator.model

data class Dish(
    val id: String,
    val name: String,
    val portions: List<Portion>,
    val isVerification: Boolean,
    val breadUnits: Double
)
