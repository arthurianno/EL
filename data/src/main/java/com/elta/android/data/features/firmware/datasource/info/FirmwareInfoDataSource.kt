package com.elta.android.data.features.firmware.datasource.info

import com.elta.android.data.features.firmware.model.NewVersionFirmwareInfoResponse
import com.elta.android.domain.features.devices.model.GlucometerInfo
import io.reactivex.Single

interface FirmwareInfoDataSource {

    fun getFirmwareInfo(
        glucometerInfo: GlucometerInfo,
    ): Single<NewVersionFirmwareInfoResponse>
}
