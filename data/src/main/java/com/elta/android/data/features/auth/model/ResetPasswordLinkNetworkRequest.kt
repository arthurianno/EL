package com.elta.android.data.features.auth.model

import com.google.gson.annotations.SerializedName

data class ResetPasswordLinkNetworkRequest(
    @SerializedName("email") val email: String,
    @SerializedName("languageTag") val languageTag: String
)
