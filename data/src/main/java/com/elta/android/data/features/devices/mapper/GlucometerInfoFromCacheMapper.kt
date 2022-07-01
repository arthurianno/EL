package com.elta.android.data.features.devices.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.common.utils.toIsoDate
import com.elta.android.data.features.devices.cache.dto.GlucometerInfoCachedDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.dto.VersionDto
import javax.inject.Inject

class GlucometerInfoFromCacheMapper @Inject constructor() : Mapper<GlucometerInfoCachedDto, GlucometerInfoDto> {

    override fun mapFromObject(source: GlucometerInfoCachedDto): GlucometerInfoDto =
        with(source) {
            GlucometerInfoDto(
                id = secondaryId,
                deviceDate = deviceDate?.toIsoDate(),
                syncDate = syncDate?.toIsoDate(),
                temperature = temperature,
                batteryLevel = batteryLevel,
                version = VersionDto(
                    software = software,
                    hardware = hardware
                ),
                lastSyncedEvent = lastSyncedEvent
            )
        }
}
