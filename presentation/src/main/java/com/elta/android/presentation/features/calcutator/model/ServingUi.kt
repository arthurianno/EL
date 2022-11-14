package com.elta.android.presentation.features.calcutator.model

data class ServingUi(
    val id: String,
    val servingDescription: String,
    val measurementDescription: String,
    val numberOfUnits: Double,
    val calories: Double,
    val proteins: Double,
    val fats: Double,
    val carbs: Double
)
