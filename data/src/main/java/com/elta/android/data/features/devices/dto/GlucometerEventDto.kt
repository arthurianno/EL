package com.elta.android.data.features.devices.dto

import org.threeten.bp.ZonedDateTime

data class GlucometerEventDto(
    val id: String,
    val date: ZonedDateTime?,
    val temperature: Double?,
    val value: Double?,
    val glucometerSerialNumber: String?,
    val originalResponse: String?
)
