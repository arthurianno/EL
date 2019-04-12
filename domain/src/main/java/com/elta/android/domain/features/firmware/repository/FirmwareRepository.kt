package com.elta.android.domain.features.firmware.repository

import com.elta.android.domain.features.firmware.model.Firmware
import io.reactivex.Single
import java.io.File

interface FirmwareRepository {

    fun getFirmwareInfo(): Single<Firmware>

    fun getFirmware(version: String): Single<File>
}