package com.elta.android.data.features.devices.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.common.utils.toIsoString
import com.elta.android.data.features.devices.cache.dto.GlucometerInfoCachedDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import javax.inject.Inject

class GlucometerInfoToCacheMapper @Inject constructor() : Mapper<GlucometerInfoDto, GlucometerInfoCachedDto> {

    override fun mapFromObject(source: GlucometerInfoDto): GlucometerInfoCachedDto =
        with(source) {
            GlucometerInfoCachedDto(
                id = id.hashCode().toLong(),
                secondaryId = id,
                deviceDate = deviceDate?.toIsoString(),
                syncDate = syncDate?.toIsoString(),
                temperature = temperature,
                batteryLevel = batteryLevel,
                software = version?.software,
                hardware = version?.hardware,
                lastSyncedEvent = lastSyncedEvent
            )
        }
}