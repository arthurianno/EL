package com.elta.android.data.features.firmware.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.common.errors.NoSuchFirmware
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.devices.glucometer.GlucometersManager
import com.elta.android.data.features.firmware.datasource.FirmwareDataSource
import com.elta.android.data.features.firmware.dto.FirmwareDto
import com.elta.android.data.features.firmware.dto.FirmwareFileDto
import com.elta.android.domain.features.firmware.model.Firmware
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.elta.android.domain.features.firmware.repository.FirmwareRepository
import io.reactivex.Single
import javax.inject.Inject

class FirmwareDataRepository @Inject constructor(
    private val glucometersManager: GlucometersManager,
    private val firmwareToDomainMapper: Mapper<FirmwareDto, Firmware>,
    private val firmwareFileToDomainMapper: Mapper<FirmwareFileDto, FirmwareFile>,
    @Remote private val remoteSource: FirmwareDataSource,
    @Cache private val localSource: FirmwareDataSource
) : FirmwareRepository {

    override fun getFirmwareInfo(): Single<Firmware> =
        remoteSource.getFirmwareInfo()
            .map(firmwareToDomainMapper::mapFromObject)
            .map { it.copy(isCompatibleWithApplication = glucometersManager.isSupportedByApplication(it)) }

    override fun getFirmware(firmware: Firmware): Single<FirmwareFile> =
        localSource.getFirmware(firmware)
            .onErrorResumeNext { error ->
                when (error) {
                    is NoSuchFirmware -> remoteSource.getFirmware(firmware)
                    else -> Single.error(error)
                }
            }
            .map(firmwareFileToDomainMapper::mapFromObject)
}