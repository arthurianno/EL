package com.elta.android.domain.features.devices.model

import java.util.Date

data class GlucometerInfo(
    val deviceDate: Date? = null,
    val temperature: Int? = null,
    val batteryLevel: Int? = null,
    val softwareVersion: Double? = null,
    val hardwareVersion: Double? = null
)