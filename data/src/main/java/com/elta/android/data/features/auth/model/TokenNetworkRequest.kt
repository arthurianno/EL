package com.elta.android.data.features.auth.model

import com.google.gson.annotations.SerializedName

data class TokenNetworkRequest(
    @SerializedName("token") val token: String
)
