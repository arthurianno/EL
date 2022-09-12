package com.elta.android.data.features.firmware.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.firmware.dto.FirmwareDto
import com.elta.android.domain.features.firmware.model.Firmware
import javax.inject.Inject

class FirmwareToDomainMapper @Inject constructor() : Mapper<FirmwareDto, Firmware> {
    override fun mapFromObject(source: FirmwareDto): Firmware =
        with(source) {
            Firmware(
                version = actual.version,
                compatible = compatible,
                hash = actual.hash
            )
        }
}
