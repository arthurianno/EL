package com.elta.android.presentation.features.calcutator.model

data class DishUi(
    val id: String,
    val name: String,
    val ration: String,
    val rationCount: Double,
    val isVerification: Boolean,
    val calories: Int,
    val proteins: Int,
    val fats: Int,
    val carbs: Int,
    val xe: Int
)
