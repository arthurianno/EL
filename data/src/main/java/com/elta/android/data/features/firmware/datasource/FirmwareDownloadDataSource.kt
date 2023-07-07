package com.elta.android.data.features.firmware.datasource

import com.elta.android.data.features.firmware.model.FirmwareFileStorageEntity
import com.elta.android.domain.features.firmware.model.FirmwareInfo
import io.reactivex.Single

interface FirmwareDownloadDataSource {

    fun downloadFirmware(firmwareInfo: FirmwareInfo): Single<FirmwareFileStorageEntity>
}
