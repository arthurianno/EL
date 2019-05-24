package com.elta.android.data.features.firmware.datasource

import com.elta.android.common.errors.NoSuchFirmware
import com.elta.android.data.features.firmware.dto.FirmwareDto
import com.elta.android.data.features.firmware.dto.FirmwareFileDto
import com.elta.android.domain.features.firmware.model.Firmware
import io.reactivex.Single
import javax.inject.Inject

class FirmwareLocalDataSource @Inject constructor(
    private val firmwaresManager: FirmwaresManager
) : FirmwareDataSource {

    override fun getFirmwareInfo(): Single<FirmwareDto> {
        throw UnsupportedOperationException("${this.javaClass.simpleName} doesn't support getFirmwareInfo.")
    }

    override fun getFirmware(firmware: Firmware): Single<FirmwareFileDto> =
        Single.fromCallable {
            firmwaresManager.getFile(firmware.version)?.let { file ->
                firmware.toFirmwareFileDto(file)
            } ?: throw NoSuchFirmware
        }.validateFileHash(firmware, NoSuchFirmware)
}