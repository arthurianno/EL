package com.elta.android.data.features.sale_points.dto

import com.google.gson.annotations.SerializedName

data class CoordinatesDto(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)
