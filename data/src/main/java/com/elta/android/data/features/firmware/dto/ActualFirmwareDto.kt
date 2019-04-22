package com.elta.android.data.features.firmware.dto

import com.google.gson.annotations.SerializedName

data class ActualFirmwareDto(
    @SerializedName("version") val version: String,
    @SerializedName("size") val size: Int,
    @SerializedName("hash") val hash: String
)