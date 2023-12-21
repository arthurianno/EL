package com.elta.android.data.features.calculator.model

import com.google.gson.annotations.SerializedName

data class StoredProductNetworkEntity(
    @SerializedName("foodId")
    val foodId: String,
    @SerializedName("foodName")
    val foodName: String,
    @SerializedName("servings")
    val servings: List<Servings>,
) {
    data class Servings(
        @SerializedName("servingId")
        val servingId: String,
        @SerializedName("carbohydrate")
        val carbohydrate: Int?,
        @SerializedName("metricServingUnit")
        val metricServingUnit: MetricServingUnitResponse,
        @SerializedName("metricServingAmount")
        val metricServingAmount: Double,
        @SerializedName("calories")
        val calories: Int?,
        @SerializedName("fat")
        val fat: Int?,
        @SerializedName("protein")
        val protein: Int?
    )
}
