package com.elta.android.data.features.calculator.model

import com.google.gson.annotations.SerializedName

data class CompactFoodNetworkEntity(
    @SerializedName("food_description") val foodDescription: String,
    @SerializedName("food_id") val foodId: String,
    @SerializedName("food_name") val foodName: String,
    @SerializedName("food_type") val foodType: String,
    @SerializedName("food_url") val foodUrl: String?,
    @SerializedName("brand_name") val brandName: String?
)
