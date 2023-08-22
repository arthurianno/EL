package com.elta.android.data.features.calculator.model

import com.google.gson.annotations.SerializedName

data class FoodBrandResponse(
    @SerializedName("food") val food: Food
) {
    data class Food(
        @SerializedName("food_id") val foodId: String,
        @SerializedName("food_name") val foodName: String,
        @SerializedName("food_type") val foodType: String,
        @SerializedName("brand_name") val brandName: String?,
        @SerializedName("food_url") val foodUrl: String?,
        @SerializedName("servings") val servingsBrand: ServingBrand
    ) {
        data class ServingBrand(
            @SerializedName("serving") val serving: ServingNetworkEntity
        )
    }
}
