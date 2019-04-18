package com.elta.android.data.features.firmware.dto

import com.google.gson.annotations.SerializedName

data class FirmwareDto(
    @SerializedName("actual") val actual: ActualFirmwareDto,
    @SerializedName("compatible") val compatible: String
)