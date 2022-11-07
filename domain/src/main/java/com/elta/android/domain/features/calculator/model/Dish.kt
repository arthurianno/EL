package com.elta.android.domain.features.calculator.model

data class Dish(
    val id: String,
    val name: String,
    val portionDescription: String,
    val portionCount: Double,
    val isVerification: Boolean,
    val calories: Int,
    val proteins: Int,
    val fats: Int,
    val carbs: Int,
    val breadUnits: Int
)
