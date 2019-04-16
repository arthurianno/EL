package com.elta.android.data.features.firmware.mapper

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.firmware.dto.FirmwareFileDto
import com.elta.android.domain.features.firmware.model.FirmwareFile
import javax.inject.Inject

class FirmwareFileToDomainMapper @Inject constructor() : Mapper<FirmwareFileDto, FirmwareFile> {
    override fun mapFromObject(source: FirmwareFileDto): FirmwareFile =
        with(source) {
            FirmwareFile(
                compatible = compatible,
                path = path,
                hash = hash
            )
        }
}
