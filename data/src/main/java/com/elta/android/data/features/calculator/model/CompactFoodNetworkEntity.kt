package com.elta.android.data.features.calculator.model

import com.google.gson.annotations.SerializedName

data class CompactFoodNetworkEntity(
    @SerializedName("brand_name") val brandName: String?,
    @SerializedName("food_id") val foodId: String,
    @SerializedName("food_name") val foodName: String,
    @SerializedName("food_sub_categories") val foodSubCategories: FoodSubCategories?,
    @SerializedName("food_type") val foodType: String,
    @SerializedName("food_url") val foodUrl: String?,
    @SerializedName("servings") val servings: Servings
) {

    data class Servings(
        @SerializedName("serving") val serving: List<Serving>
    )
    data class Serving(
        @SerializedName("serving_id") val servingId: String,
        @SerializedName("serving_description") val servingDescription: String,
        @SerializedName("serving_url") val servingUrl: String?,
        @SerializedName("number_of_units") val numberOfUnits: String,
        @SerializedName("measurement_description") val measurementDescription: String,
        @SerializedName("is_default") val isDefault: String,
        @SerializedName("calories") val calories: String,
        @SerializedName("carbohydrate") val carbohydrate: String,
        @SerializedName("protein") val protein: String,
        @SerializedName("fat") val fat: String,
        @SerializedName("saturated_fat") val saturatedFat: String?,
        @SerializedName("trans_fat") val transFat: String?,
        @SerializedName("cholesterol") val cholesterol: String?,
        @SerializedName("sodium") val sodium: String?,
        @SerializedName("potassium") val potassium: String?,
        @SerializedName("fiber") val fiber: String?,
        @SerializedName("sugar") val sugar: String?,
        @SerializedName("added_sugars") val addedSugars: String?,
        @SerializedName("calcium") val calcium: String?,
        @SerializedName("iron") val iron: String?,
        @SerializedName("metric_serving_amount") val metricServingAmount: String?,
        @SerializedName("metric_serving_unit") val metricServingUnit: String?,
        @SerializedName("polyunsaturated_fat") val polyunsaturatedFat: String?,
        @SerializedName("monounsaturated_fat") val monounsaturatedFat: String?,
        @SerializedName("vitamin_d") val vitaminD: String?,
        @SerializedName("vitamin_a") val vitaminA: String,
        @SerializedName("vitamin_c") val vitaminC: String?


    )

    data class FoodSubCategories(
        @SerializedName("food_sub_category") val foodSubCategory: List<String>
    )
}
