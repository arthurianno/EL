package com.elta.android.data.features.devices.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.domain.features.devices.model.GlucometerInfo
import javax.inject.Inject

class GlucometerInfoToDomainMapper @Inject constructor() : Mapper<GlucometerInfoDto, GlucometerInfo> {

    override fun mapFromObject(source: GlucometerInfoDto): GlucometerInfo =
        with(source) {
            GlucometerInfo(
                deviceDate = deviceDate,
                temperature = temperature,
                batteryLevel = batteryLevel,
                softwareVersion = version?.software,
                hardwareVersion = version?.hardware
            )
        }
}