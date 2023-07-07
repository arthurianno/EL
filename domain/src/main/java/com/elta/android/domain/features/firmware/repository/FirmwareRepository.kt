package com.elta.android.domain.features.firmware.repository

import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.elta.android.domain.features.firmware.model.FirmwareInfo
import io.reactivex.Single

interface FirmwareRepository {

    fun getFirmwareInfo(glucometerInfo: GlucometerInfo): Single<FirmwareInfo>

    fun downloadFirmware(firmwareInfo: FirmwareInfo): Single<FirmwareFile>
}
