package com.elta.android.presentation.features.calcutator.products.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.elta.android.domain.features.calculator.model.DishType
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class DishUiEntity(
    val id: String,
    val localId: String,
    val name: String,
    val type: DishType,
    val brandName: String,
    val isVerified: Boolean,
    val servings: List<ServingUiEntity>,
    val servingSelect: ServingUiEntity,
    val servingAmount: String,
    val servingCalories: Pair<String, String>,
    val breadUnits: String?
) : Parcelable
