package com.elta.android.data.features.calculator.dto

import com.google.gson.annotations.SerializedName

data class CompactFoodDto(
    @SerializedName("food_description") val foodDescription: String,
    @SerializedName("food_id") val foodId: String,
    @SerializedName("food_name") val foodName: String,
    @SerializedName("food_type") val foodType: String,
    @SerializedName("food_url") val foodUrl: String,
    @SerializedName("brand_name") val brandName: String
)
