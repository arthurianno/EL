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
    val isVerification: Boolean,
    val servings: List<ServingUiEntity>,
    val servingSelect: ServingUiEntity,
    val servingAmount: String,
    val servingCalories: Pair<String, String>,
    val breadUnits: String
) : Parcelable
