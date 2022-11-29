package com.elta.android.data.features.calculator.model

import com.google.gson.annotations.SerializedName

data class FoodGenericResponse(
    @SerializedName("food") val food: Food
) {
    data class Food(
        @SerializedName("food_id") val foodId: String,
        @SerializedName("food_name") val foodName: String,
        @SerializedName("food_type") val foodType: String,
        @SerializedName("brand_name") val brandName: String?,
        @SerializedName("food_url") val foodUrl: String,
        @SerializedName("servings") val servingsGeneric: ServingsGeneric
    ) {
        data class ServingsGeneric(
            @SerializedName("serving") val servings: List<ServingNetworkEntity>
        )
    }
}
