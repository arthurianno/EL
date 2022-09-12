package com.elta.android.data.features.auth.api.request

import com.google.gson.annotations.SerializedName

data class SocialNetworkRequest(
    @SerializedName("token") val token: String
)
