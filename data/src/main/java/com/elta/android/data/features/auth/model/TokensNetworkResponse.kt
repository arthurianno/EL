package com.elta.android.data.features.auth.model

import com.google.gson.annotations.SerializedName

data class TokensNetworkResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)
