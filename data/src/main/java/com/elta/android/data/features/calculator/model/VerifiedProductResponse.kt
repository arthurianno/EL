package com.elta.android.data.features.calculator.model


import com.google.gson.annotations.SerializedName

data class VerifiedProductResponse(
    @SerializedName("isVerified")
    val isVerified: Boolean,
    @SerializedName("food_id")
    val foodId: String,
    @SerializedName("food_name")
    val foodName: String,
    @SerializedName("servings")
    val servings: List<Serving>
) {
    data class Serving(
        @SerializedName("serving_id")
        val servingId: String,

        @SerializedName("calories")
        val calories: Double?,

        @SerializedName("carbohydrate")
        val carbohydrate: Double,

        @SerializedName("fat")
        val fat: Double?,

        @SerializedName("protein")
        val protein: Double?,

        @SerializedName("metric_serving_amount")
        val metricServingAmount: Double,

        @SerializedName("metric_serving_unit")
        val metricServingUnit: String,
    )
}