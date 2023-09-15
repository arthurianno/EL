package com.elta.android.data.features.user.dto

import com.google.gson.annotations.SerializedName

data class SocialNetworkDto(
    @SerializedName("foodName") val type: SocialNetworkTypeNetworkEntity,
    @SerializedName("isLinked") val isLinked: Boolean
)
