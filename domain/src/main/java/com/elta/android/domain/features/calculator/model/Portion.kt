package com.elta.android.domain.features.calculator.model

data class Portion(
    val id: String,
    val description: String,
    val calories: Double,
    val proteins: Double,
    val fats: Double,
    val carbs: Double
)
