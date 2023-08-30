package com.elta.android.presentation.features.calcutator.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServingUiEntity(
    val id: String,
    val servingDescription: String,
    val numberOfUnits: String,
    val calories: String,
    val protein: String,
    val fat: String,
    val carbohydrate: String
) : Parcelable
