package com.elta.android.data.features.auth.model

import com.google.gson.annotations.SerializedName

data class AuthNetworkRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)
