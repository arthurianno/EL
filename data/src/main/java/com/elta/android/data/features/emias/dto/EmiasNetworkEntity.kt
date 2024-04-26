package com.elta.android.data.features.emias.dto

import com.google.gson.annotations.SerializedName

data class EmiasNetworkEntity(
    @SerializedName("oms") val oms: String,
    @SerializedName("birthDate") val birthDate: String
)
