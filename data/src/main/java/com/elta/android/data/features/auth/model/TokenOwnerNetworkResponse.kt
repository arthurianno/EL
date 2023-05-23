package com.elta.android.data.features.auth.model

import com.google.gson.annotations.SerializedName

data class TokenOwnerNetworkResponse(
    @SerializedName("isOwner") val isOwner: Boolean
)
