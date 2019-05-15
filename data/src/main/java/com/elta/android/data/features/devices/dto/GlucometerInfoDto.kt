package com.elta.android.data.features.devices.dto

import java.util.Date

data class GlucometerInfoDto(
    val id: String,
    val deviceDate: Date? = null,
    val syncDate: Date? = null,
    val temperature: Int? = null,
    val batteryLevel: Int? = null,
    val version: VersionDto? = null
)