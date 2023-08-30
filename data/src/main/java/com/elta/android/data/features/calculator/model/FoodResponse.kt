package com.elta.android.data.features.calculator.model

import com.google.gson.annotations.SerializedName

data class FoodResponse(
    @SerializedName("food") val food: List<FoodNetworkEntity>?,
)