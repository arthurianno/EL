package com.elta.android.presentation.features.calcutator.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServingUiEntity(
    val id: String,
    val servingDescription: String,
    val numberOfUnits: Double,
    val calories: Double,
    val proteins: Double,
    val fats: Double,
    val carbs: Double
) : Parcelable
