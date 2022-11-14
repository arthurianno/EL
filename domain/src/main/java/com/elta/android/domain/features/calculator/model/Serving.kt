package com.elta.android.domain.features.calculator.model

data class Serving(
    val id: String,
    val servingDescription: String,
    val measurementDescription: String,
    val numberOfUnits: Double,
    val calories: Double,
    val proteins: Double,
    val fats: Double,
    val carbs: Double
) {

    companion object {
        fun empty(): Serving = Serving(
            id = "",
            servingDescription = "",
            measurementDescription = "",
            numberOfUnits = 0.0,
            calories = 0.0,
            proteins = 0.0,
            fats = 0.0,
            carbs = 0.0
        )
    }
}
