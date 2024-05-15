package com.elta.android.data.features.firmware.datasource

import com.elta.android.common.errors.FirmwareDownloadingError
import com.elta.android.data.features.firmware.api.FirmwareApi
import com.elta.android.data.features.firmware.model.FirmwareFileStorageEntity
import com.elta.android.data.features.firmware.toFirmwareFileStorage
import com.elta.android.data.features.firmware.validateFileHash
import com.elta.android.domain.features.firmware.model.FirmwareInfo
import io.reactivex.Single
import javax.inject.Inject

class FirmwareRemoteDownloadDataSource @Inject constructor(
    private val firmwaresManager: FirmwaresManager,
    private val api: FirmwareApi
) : FirmwareDownloadDataSource {

    override fun downloadFirmware(firmwareInfo: FirmwareInfo): Single<FirmwareFileStorageEntity> =
        api.downloadModelFirmware(firmwareInfo.id)
            .map { body ->
                firmwaresManager.writeToFile(firmwareInfo.version, body)?.let { file ->
                    firmwareInfo.toFirmwareFileStorage(file)
                } ?: throw FirmwareDownloadingError
            }.validateFileHash(firmwareInfo, FirmwareDownloadingError)
            .onErrorResumeNext { Single.error(FirmwareDownloadingError) }
}
