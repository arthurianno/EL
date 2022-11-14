package com.elta.android.data.features.calculator.dto

import com.google.gson.annotations.SerializedName

data class FoodGenericDto(
    @SerializedName("food") val food: Food
) {
    data class Food(
        @SerializedName("food_id") val foodId: String,
        @SerializedName("food_name") val foodName: String,
        @SerializedName("food_type") val foodType: String,
        @SerializedName("food_url") val foodUrl: String,
        @SerializedName("servings") val servingsGeneric: ServingGenericDto
    )
}
