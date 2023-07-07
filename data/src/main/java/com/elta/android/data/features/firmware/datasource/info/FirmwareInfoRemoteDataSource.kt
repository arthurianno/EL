package com.elta.android.data.features.firmware.datasource.info

import com.elta.android.data.features.firmware.api.FirmwareApi
import com.elta.android.data.features.firmware.model.NewVersionFirmwareInfoResponse
import com.elta.android.domain.features.devices.model.GlucometerInfo
import io.reactivex.Single
import javax.inject.Inject

class FirmwareInfoRemoteDataSource @Inject constructor(
    private val api: FirmwareApi
): FirmwareInfoDataSource {

    override fun getFirmwareInfo(
        glucometerInfo: GlucometerInfo
    ): Single<NewVersionFirmwareInfoResponse> =
        api.getFirmwareInfo(
            mac = glucometerInfo.id,
            serialNumber = glucometerInfo.glucometerSerialNumber,
            hardwareVersion = glucometerInfo.hardwareVersion,
            firmwareVersion = glucometerInfo.softwareVersion
        )
}