package com.elta.android.data.features.firmware.repository

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.firmware.datasource.FirmwareDataSource
import com.elta.android.data.features.firmware.dto.FirmwareDto
import com.elta.android.data.features.firmware.dto.FirmwareFileDto
import com.elta.android.domain.features.firmware.model.Firmware
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.elta.android.domain.features.firmware.repository.FirmwareRepository
import io.reactivex.Single
import javax.inject.Inject

class FirmwareDataRepository @Inject constructor(
    private val firmwareToDomainMapper: Mapper<FirmwareDto, Firmware>,
    private val firmwareFileToDomainMapper: Mapper<FirmwareFileDto, FirmwareFile>,
    private val source: FirmwareDataSource
) : FirmwareRepository {

    override fun getFirmwareInfo(): Single<Firmware> =
        source.getFirmwareInfo().map(firmwareToDomainMapper::mapFromObject)

    override fun getFirmware(firmware: Firmware): Single<FirmwareFile> =
        source.getFirmware(firmware).map(firmwareFileToDomainMapper::mapFromObject)
}