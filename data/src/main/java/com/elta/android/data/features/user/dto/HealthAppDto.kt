package com.elta.android.data.features.user.dto

import com.google.gson.annotations.SerializedName

data class HealthAppDto(
    @SerializedName("type") val type: HealthAppTypeDto,
    @SerializedName("isActive") val isActive: Boolean
)