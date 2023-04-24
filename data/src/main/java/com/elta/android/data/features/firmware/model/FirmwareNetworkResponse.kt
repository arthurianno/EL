package com.elta.android.data.features.firmware.model

import com.google.gson.annotations.SerializedName

data class FirmwareNetworkResponse(
    @SerializedName("actual") val actual: ActualFirmware,
    @SerializedName("compatible") val compatible: String?
) {
    data class ActualFirmware(
        @SerializedName("id") val id: String,
        @SerializedName("version") val version: String,
        @SerializedName("size") val size: Int,
        @SerializedName("hash") val hash: String
    )
}
