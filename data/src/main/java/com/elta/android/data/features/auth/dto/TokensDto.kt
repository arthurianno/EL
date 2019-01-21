package com.elta.android.data.features.auth.dto

import com.google.gson.annotations.SerializedName

data class TokensDto(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)