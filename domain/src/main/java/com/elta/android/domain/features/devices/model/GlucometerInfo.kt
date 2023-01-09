package com.elta.android.domain.features.devices.model

import org.threeten.bp.ZonedDateTime

data class GlucometerInfo(
    val id: String,
    val deviceDate: ZonedDateTime? = null,
    val syncDate: ZonedDateTime? = null,
    val temperature: Int? = null,
    val batteryLevel: Int? = null,
    val softwareVersion: Double? = null,
    val hardwareVersion: Double? = null,
    val glucometerSerialNumber: String? = null
)
