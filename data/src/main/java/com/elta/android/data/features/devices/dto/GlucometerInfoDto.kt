package com.elta.android.data.features.devices.dto

import org.threeten.bp.ZonedDateTime

data class GlucometerInfoDto(
    val id: String,
    val deviceDate: ZonedDateTime? = null,
    val syncDate: ZonedDateTime? = null,
    val temperature: Int? = null,
    val batteryLevel: Int? = null,
    val version: VersionDto? = null,
    val lastSyncedEvent: String? = null
)
