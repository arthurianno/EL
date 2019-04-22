package com.elta.android.data.features.devices.dto

import java.util.Date

data class GlucometerEventDto(
    val id: String,
    val date: Date?,
    val temperature: Int?,
    val value: Double?
)