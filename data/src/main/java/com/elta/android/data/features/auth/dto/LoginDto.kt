package com.elta.android.data.features.auth.dto

import com.google.gson.annotations.SerializedName

data class LoginDto(
    @SerializedName("emailConfirmed") val isEmailConfirmed: Boolean,
    @SerializedName("tokens") val tokens: TokensDto
)
