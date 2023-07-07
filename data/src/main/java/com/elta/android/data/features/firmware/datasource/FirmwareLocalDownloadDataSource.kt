package com.elta.android.data.features.firmware.datasource

import com.elta.android.common.errors.NoSuchFirmware
import com.elta.android.data.features.firmware.model.FirmwareFileStorageEntity
import com.elta.android.data.features.firmware.toFirmwareFileStorage
import com.elta.android.data.features.firmware.validateFileHash
import com.elta.android.domain.features.firmware.model.FirmwareInfo
import io.reactivex.Single
import javax.inject.Inject

class FirmwareLocalDownloadDataSource @Inject constructor(
    private val firmwaresManager: FirmwaresManager
) : FirmwareDownloadDataSource {

    override fun downloadFirmware(
        firmwareInfo: FirmwareInfo
    ): Single<FirmwareFileStorageEntity> =
        Single.fromCallable {
            firmwaresManager.getFile(firmwareInfo.version)?.let { file ->
                firmwareInfo.toFirmwareFileStorage(file)
            } ?: throw NoSuchFirmware
        }.validateFileHash(firmwareInfo, NoSuchFirmware)
}
