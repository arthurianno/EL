package com.elta.android.data.features.devices.glucometer.builder

import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.dto.VersionDto
import org.threeten.bp.ZonedDateTime

interface GlucometerInfoBuilder {

    @Deprecated("Used for old synchronization and firmware updates. Use the method with other parameters.")
    fun buildFrom(
        id: String,
        params: List<String>,
        syncDate: ZonedDateTime? = null,
        lastSyncedEvent: String? = null
    ): GlucometerInfoDto

    fun buildFrom(
        id: String,
        date: ZonedDateTime?,
        temperature: Int?,
        batteryLevel: Int?,
        version: VersionDto?,
        serial: String?,
        syncDate: ZonedDateTime?,
        lastSyncedEvent: String?
    ): GlucometerInfoDto
}
