package com.elta.android.data.features.auth.model

import com.google.gson.annotations.SerializedName

data class ChangePasswordNetworkRequest(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("newPassword") val newPassword: String
)
