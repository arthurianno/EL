package com.elta.android.data.features.calculator.model

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("productId") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("productType") val type: String,
    @SerializedName("servingAmount") val servingAmount: Double,
    @SerializedName("servingId") val servingId: String,
    @SerializedName("servingName") val servingName: String,
    @SerializedName("breadUnits") val breadUnits: Double
)
