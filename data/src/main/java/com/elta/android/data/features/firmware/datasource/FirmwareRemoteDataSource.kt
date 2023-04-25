package com.elta.android.data.features.firmware.datasource

import com.elta.android.common.errors.FirmwareDownloadingError
import com.elta.android.data.features.firmware.api.FirmwareApi
import com.elta.android.data.features.firmware.model.FirmwareFileStorageEntity
import com.elta.android.data.features.firmware.model.FirmwareNetworkResponse
import com.elta.android.data.features.firmware.toFirmwareFileStorage
import com.elta.android.data.features.firmware.validateFileHash
import com.elta.android.domain.features.firmware.model.Firmware
import io.reactivex.Single
import javax.inject.Inject

class FirmwareRemoteDataSource @Inject constructor(
    private val firmwaresManager: FirmwaresManager,
    private val api: FirmwareApi
) : FirmwareDataSource {

    override fun getFirmwareInfo(): Single<FirmwareNetworkResponse> =
        api.getFirmwareInfo()

    override fun getFirmware(firmware: Firmware): Single<FirmwareFileStorageEntity> =
        api.downloadFirmware(firmware.version)
            .map { body ->
                firmwaresManager.writeToFile(firmware.version, body)?.let { file ->
                    firmware.toFirmwareFileStorage(file)
                } ?: throw FirmwareDownloadingError
            }.validateFileHash(firmware, FirmwareDownloadingError)

    override fun getModelFirmwareInfo(modelId: String): Single<FirmwareNetworkResponse> =
        api.getModelFirmwareInfo(modelId)

    override fun getModelFirmware(firmware: Firmware, modelId: String): Single<FirmwareFileStorageEntity> =
        api.downloadModelFirmware(modelId = modelId, version = firmware.version)
            .map { body ->
                firmwaresManager.writeToFile(firmware.version, body)?.let { file ->
                    firmware.toFirmwareFileStorage(file)
                } ?: throw FirmwareDownloadingError
            }.validateFileHash(firmware, FirmwareDownloadingError)
}
