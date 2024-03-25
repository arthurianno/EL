package com.elta.android.data.features.devices.glucometer.builder

import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.dto.VersionDto
import com.elta.android.domain.features.devices.model.GlucometerInfo
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class DefaultGlucometerInfoBuilder @Inject constructor() : GlucometerInfoBuilder {

    override fun buildFrom(
        glucometerInfo: GlucometerInfo,
        lastSyncedEvent: String?
    ): GlucometerInfoDto {

        return GlucometerInfoDto(
            id = glucometerInfo.id,
            deviceDate = glucometerInfo.deviceDate,
            temperature = glucometerInfo.temperature,
            batteryLevel = glucometerInfo.batteryLevel,
            version = VersionDto(software = glucometerInfo.softwareVersion, hardware = glucometerInfo.hardwareVersion),
            glucometerSerialNumber = glucometerInfo.glucometerSerialNumber,
            syncDate = ZonedDateTime.now(),
            lastSyncedEvent = lastSyncedEvent
        )
    }
}
