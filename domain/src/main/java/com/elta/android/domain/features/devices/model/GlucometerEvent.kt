package com.elta.android.domain.features.devices.model

import org.threeten.bp.ZonedDateTime

data class GlucometerEvent(
    val id: String,
    val date: ZonedDateTime?,
    val temperature: Double?,
    val value: Double?,
    val glucometerSerialNumber: String?,
    val originalResponse: String?
)