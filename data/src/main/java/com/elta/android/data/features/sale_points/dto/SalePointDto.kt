package com.elta.android.data.features.sale_points.dto

import com.google.gson.annotations.SerializedName

data class SalePointDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: TypeDto,
    @SerializedName("region") val region: String,
    @SerializedName("city") val city: String,
    @SerializedName("address") val address: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("coordinates") val coordinates: CoordinatesDto,
    @SerializedName("timeStamp") val timeStamp: Int,
    @SerializedName("modifiedState") val modifiedState: StateDto
)