package com.elta.android.data.features.firmware.datasource

import com.elta.android.common.errors.NoSuchFirmware
import com.elta.android.data.features.firmware.model.FirmwareFileStorageEntity
import com.elta.android.data.features.firmware.model.FirmwareNetworkResponse
import com.elta.android.data.features.firmware.toFirmwareFileStorage
import com.elta.android.data.features.firmware.validateFileHash
import com.elta.android.domain.features.firmware.model.Firmware
import io.reactivex.Single
import javax.inject.Inject

class FirmwareLocalDataSource @Inject constructor(
    private val firmwaresManager: FirmwaresManager
) : FirmwareDataSource {

    override fun getFirmwareInfo(): Single<FirmwareNetworkResponse> {
        throw UnsupportedOperationException("${this.javaClass.simpleName} doesn't support getFirmwareInfo.")
    }

    override fun getFirmware(firmware: Firmware): Single<FirmwareFileStorageEntity> =
        Single.fromCallable {
            firmwaresManager.getFile(firmware.version)?.let { file ->
                firmware.toFirmwareFileStorage(file)
            } ?: throw NoSuchFirmware
        }.validateFileHash(firmware, NoSuchFirmware)

    override fun getModelFirmwareInfo(modelId: String): Single<FirmwareNetworkResponse> {
        throw UnsupportedOperationException("${this.javaClass.simpleName} doesn't support getFirmwareInfo.")
    }

    override fun getModelFirmware(
        firmware: Firmware,
        modelId: String
    ): Single<FirmwareFileStorageEntity> =
        Single.fromCallable {
            firmwaresManager.getFile(firmware.version)?.let { file ->
                firmware.toFirmwareFileStorage(file)
            } ?: throw NoSuchFirmware
        }.validateFileHash(firmware, NoSuchFirmware)
}
