package com.elta.android.data.features.glucometers.dto

import com.google.gson.annotations.SerializedName

data class GlucometerNetworkEntity(
    @SerializedName("primaryGlucometer") val primaryGlucometer: GlucometerInfoNetworkEntity?,
    @SerializedName("secondaryGlucometers") val secondaryGlucometers: List<GlucometerInfoNetworkEntity>,
) {
    data class GlucometerInfoNetworkEntity(
        @SerializedName("serialNumber") val serialNumber: String,
        @SerializedName("hardwareVersion") val hardwareVersion: String,
        @SerializedName("firmwareVersion") val firmwareVersion: String,
        @SerializedName("mac") val mac: String
    )
}
