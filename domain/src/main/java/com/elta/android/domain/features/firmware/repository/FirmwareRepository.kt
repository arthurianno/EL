package com.elta.android.domain.features.firmware.repository

import com.elta.android.domain.features.firmware.model.Firmware
import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Single

interface FirmwareRepository {

    fun getFirmwareInfo(): Single<Firmware>

    fun downloadFirmware(firmware: Firmware): Single<FirmwareFile>
}