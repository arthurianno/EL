package com.elta.android.data.features.auth.api.request

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    @SerializedName("token") val token: String,
    @SerializedName("password") val newPassword: String
)
