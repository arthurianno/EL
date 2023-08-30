package com.elta.android.data.features.calculator.model

import com.google.gson.annotations.SerializedName

data class FoodNetworkEntity(
    @SerializedName("brand_name") val brandName: String?,
    @SerializedName("food_id") val foodId: String,
    @SerializedName("food_name") val foodName: String,
    @SerializedName("food_sub_categories") val foodSubCategories: FoodSubCategories?,
    @SerializedName("food_type") val foodType: String,
    @SerializedName("food_url") val foodUrl: String?,
    @SerializedName("servings") val servings: Servings
) {

    data class Servings(
        @SerializedName("serving") val servings: List<ServingNetworkEntity>?
    )


    data class FoodSubCategories(
        @SerializedName("food_sub_category") val foodSubCategory: List<String>
    )
}