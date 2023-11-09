package com.elta.android.presentation.features.calcutator.products.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServingUiEntity(
    val id: String,
    val idMetricServing: Int,
    val nameMetricServing: String,
    val numberOfUnits: String,
    val calories: String,
    val protein: String,
    val fat: String,
    val carbohydrate: String
) : Parcelable
