package com.elta.android.data.features.auth.dto

import com.google.gson.annotations.SerializedName

data class TokenOwnerDto(
    @SerializedName("isOwner") val isOwner: Boolean
)
