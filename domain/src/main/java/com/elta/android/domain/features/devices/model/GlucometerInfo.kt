package com.elta.android.domain.features.devices.model

import java.util.Date

data class GlucometerInfo(
    val date: Date?,
    val temperature: Double?,
    val batteryLevel: Int?,
    val softwareVersion: Double?,
    val hardwareVersion: Double?
)