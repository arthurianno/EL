package com.elta.android.data.features.firmware.datasource

import com.elta.android.data.features.firmware.dto.FirmwareDto
import com.elta.android.data.features.firmware.dto.FirmwareFileDto
import com.elta.android.domain.features.firmware.model.Firmware
import io.reactivex.Single

interface FirmwareDataSource {

    fun getFirmwareInfo(): Single<FirmwareDto>

    fun getFirmware(firmware: Firmware): Single<FirmwareFileDto>
}