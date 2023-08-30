package com.elta.android.domain.features.calculator.model

data class Serving(
    val id: String,
    val calories: Double,
    val proteins: Double,
    val fats: Double,
    val carbohydrate: Double,
    val servingDescription: String,
    val numberOfUnits: Double,
) {

    companion object {
        fun empty(): Serving = Serving(
            id = "",
            servingDescription = "",
            numberOfUnits = 0.0,
            calories = 0.0,
            proteins = 0.0,
            fats = 0.0,
            carbohydrate = 0.0
        )
    }
}
