package com.elta.android.data.features.firmware.datasource

import com.elta.android.data.features.firmware.model.FirmwareFileStorageEntity
import com.elta.android.data.features.firmware.model.FirmwareNetworkResponse
import com.elta.android.domain.features.firmware.model.Firmware
import io.reactivex.Single

interface FirmwareDataSource {

    fun getFirmwareInfo(): Single<FirmwareNetworkResponse>
    fun getFirmware(firmware: Firmware): Single<FirmwareFileStorageEntity>
    fun getModelFirmwareInfo(modelId: String): Single<FirmwareNetworkResponse>
    fun getModelFirmware(firmware: Firmware, modelId: String): Single<FirmwareFileStorageEntity>
}
