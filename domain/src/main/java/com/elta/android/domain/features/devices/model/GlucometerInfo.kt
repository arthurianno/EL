package com.elta.android.domain.features.devices.model

import org.threeten.bp.ZonedDateTime

data class GlucometerInfo(
    val id: String,
    val deviceDate: ZonedDateTime? = null,
    val syncDate: ZonedDateTime? = null,
    val temperature: Int? = null,
    val batteryLevel: Int? = null,
    val softwareVersion: String? = null,
    val hardwareVersion: String? = null,
    val glucometerSerialNumber: String? = null
)
