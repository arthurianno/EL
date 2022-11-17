package com.elta.android.domain.features.calculator.model

data class Dish(
    val id: String,
    val localId: String,
    val name: String,
    val type: DishType,
    val servings: List<Serving>,
    val servingSelect: Serving,
    val servingAmount: Double,
    val isVerification: Boolean,
    val breadUnits: Double
)
