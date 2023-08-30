package com.elta.android.presentation.features.calcutator.model

import android.os.Parcelable
import com.elta.android.domain.features.calculator.model.DishType
import kotlinx.parcelize.Parcelize

@Parcelize
data class DishUiEntity(
    val id: String,
    val localId: String,
    val name: String,
    val type: DishType,
    val brandName: String,
    val servings: List<ServingUiEntity>,
    val servingSelect: ServingUiEntity,
    val servingAmount: Double,
    val servingCalories: Pair<String, Double>,
    val breadUnits: Double
) : Parcelable
