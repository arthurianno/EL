package com.elta.android.data.features.auth.dto

import com.google.gson.annotations.SerializedName

data class EmailStatusDto(
    @SerializedName("emailConfirmed") val isEmailConfirmed: Boolean
)