package com.elta.android.data.features.calculator.model

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("productId") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("productType") val type: String,
    @SerializedName("servingAmount") val servingAmount: Double,
    @SerializedName("servingId") val servingId: String,
    @SerializedName("servingName") val servingName: String,
    @SerializedName("breadUnits") val breadUnits: Double?,
    @SerializedName("brandName") val brandName: String,
    @SerializedName("calories") val calories: Double?,
    @SerializedName("proteins") val proteins: Double?,
    @SerializedName("fats") val fats: Double?,
    @SerializedName("carbohydrates") val carbohydrates: Double?,
    @SerializedName("isVerified") val isVerified: Boolean?,
    )
