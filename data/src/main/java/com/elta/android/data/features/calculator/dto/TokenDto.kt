package com.elta.android.data.features.calculator.dto

import com.google.gson.annotations.SerializedName

data class TokenDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in") val expiresIn: Int,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("scope") val scope: String
)
