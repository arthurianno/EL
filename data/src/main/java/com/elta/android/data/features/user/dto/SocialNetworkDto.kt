package com.elta.android.data.features.user.dto

import com.google.gson.annotations.SerializedName

data class SocialNetworkDto(
    @SerializedName("name") val type: SocialNetworkTypeDto,
    @SerializedName("isLinked") val isLinked: Boolean
)
