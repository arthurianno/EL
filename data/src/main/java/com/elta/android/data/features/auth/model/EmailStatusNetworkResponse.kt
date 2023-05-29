package com.elta.android.data.features.auth.model

import com.google.gson.annotations.SerializedName

data class EmailStatusNetworkResponse(
    @SerializedName("emailConfirmed") val isEmailConfirmed: Boolean
)
