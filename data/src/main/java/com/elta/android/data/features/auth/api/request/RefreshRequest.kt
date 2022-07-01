package com.elta.android.data.features.auth.api.request

import com.google.gson.annotations.SerializedName

data class RefreshRequest(
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("refreshToken") val refreshToken: String?
)
