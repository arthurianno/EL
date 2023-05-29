package com.elta.android.data.features.auth.model

import com.google.gson.annotations.SerializedName

data class LoginNetworkResponse(
    @SerializedName("emailConfirmed") val isEmailConfirmed: Boolean,
    @SerializedName("tokens") val tokens: TokensNetworkResponse
)
