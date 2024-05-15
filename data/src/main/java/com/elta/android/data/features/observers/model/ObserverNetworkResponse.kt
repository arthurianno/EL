package com.elta.android.data.features.observers.model

import com.google.gson.annotations.SerializedName

data class ObserverNetworkResponse(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String?,
    @SerializedName("customName") val customName: String?,
    @SerializedName("status") val status: ObserverStatusNetworkEntity,
    @SerializedName("id") val id: String,
)
