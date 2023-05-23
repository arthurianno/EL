package com.elta.android.data.features.user.dto

import com.google.gson.annotations.SerializedName

data class GlucoseLevelNetworkEntity(
    @SerializedName("min") val minValue: Double?,
    @SerializedName("max") val maxValue: Double?
)
