package com.elta.android.data.features.auth.model

import com.google.gson.annotations.SerializedName

data class ResetPasswordNetworkRequest(
    @SerializedName("token") val token: String,
    @SerializedName("password") val newPassword: String
)
