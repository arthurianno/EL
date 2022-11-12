package com.elta.android.presentation.features.calcutator.model

data class PortionUi(
    val id: String,
    val description: String,
    val metricUnit: String,
    val metricAmount: Double,
    val calories: Double,
    val proteins: Double,
    val fats: Double,
    val carbs: Double
)
