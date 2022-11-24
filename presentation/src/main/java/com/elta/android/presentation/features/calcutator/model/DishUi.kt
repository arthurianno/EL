package com.elta.android.presentation.features.calcutator.model

import android.os.Parcelable
import com.elta.android.domain.features.calculator.model.DishType
import kotlinx.parcelize.Parcelize

@Parcelize
data class DishUi(
    val id: String,
    val localId: String,
    val name: String,
    val type: DishType,
    val servings: List<ServingUi>,
    val servingSelect: ServingUi,
    val servingAmount: Double,
    val isVerification: Boolean,
    val breadUnits: Double
) : Parcelable
