package com.elta.android.data.features.user.dto

import com.google.gson.annotations.SerializedName

data class HealthAppNetworkEntity(
    @SerializedName("type") val type: HealthAppTypeNetworkEntity,
    @SerializedName("isActive") val isActive: Boolean
)
