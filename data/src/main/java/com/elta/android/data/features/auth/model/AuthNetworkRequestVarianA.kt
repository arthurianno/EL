package com.elta.android.data.features.auth.model

import com.google.gson.annotations.SerializedName

// fixme Variant A : recovery_account
data class AuthNetworkRequestVariantA(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)
