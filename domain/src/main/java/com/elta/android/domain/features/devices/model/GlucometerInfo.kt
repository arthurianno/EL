package com.elta.android.domain.features.devices.model

import java.util.Date

data class GlucometerInfo(
    val date: Date? = null,
    val temperature: Double? = null,
    val batteryLevel: Int? = null,
    val softwareVersion: Double? = null,
    val hardwareVersion: Double? = null
)