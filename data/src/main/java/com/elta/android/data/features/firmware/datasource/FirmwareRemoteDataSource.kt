package com.elta.android.data.features.firmware.datasource

import com.elta.android.common.errors.FirmwareDownloadingError
import com.elta.android.data.features.firmware.api.FirmwareApi
import com.elta.android.data.features.firmware.dto.FirmwareDto
import com.elta.android.data.features.firmware.dto.FirmwareFileDto
import com.elta.android.domain.features.firmware.model.Firmware
import io.reactivex.Single
import javax.inject.Inject

class FirmwareRemoteDataSource @Inject constructor(
    private val firmwaresManager: FirmwaresManager,
    private val api: FirmwareApi
) : FirmwareDataSource {

    override fun getFirmwareInfo(): Single<FirmwareDto> =
        api.getFirmwareInfo()

    override fun getFirmware(firmware: Firmware): Single<FirmwareFileDto> =
        api.downloadFirmware(firmware.version)
            .map { body ->
                firmwaresManager.writeToFile(firmware.version, body)?.let { file ->
                    firmware.toFirmwareFileDto(file)
                } ?: throw FirmwareDownloadingError
            }.validateFileHash(firmware, FirmwareDownloadingError)
}