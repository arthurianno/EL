package com.elta.android.data.features.calculator.dto

import com.google.gson.annotations.SerializedName

data class ProductDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("servingAmount") val servingAmount: Double,
    @SerializedName("servingId") val servingId: String,
    @SerializedName("servingName") val servingName: String,
    @SerializedName("breadUnits") val breadUnits: Double
)
