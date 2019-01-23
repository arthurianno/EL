package com.elta.android.data.features.auth.api.request

import com.google.gson.annotations.SerializedName

data class ResetPasswordLinkRequest(
    @SerializedName("email") val email: String
)