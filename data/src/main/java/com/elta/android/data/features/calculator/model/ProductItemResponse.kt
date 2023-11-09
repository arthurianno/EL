package com.elta.android.data.features.calculator.model

import com.google.gson.annotations.SerializedName

data class ProductItemResponse(
    @SerializedName("isVerified")
    val isVerified: Boolean,
    @SerializedName("foodId")
    val foodId: String,
    @SerializedName("foodName")
    val foodName: String,
    @SerializedName("servings")
    val servings: List<ServingResponse>
)