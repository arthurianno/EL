package com.elta.android.data.features.user.api.request

import com.google.gson.annotations.SerializedName

data class ShortUserSettingsRequest(
    @SerializedName("diabet") val diabetes: String?,
    @SerializedName("weight") val weight: Double? = 0.0,
    @SerializedName("gender") val gender: String?
)