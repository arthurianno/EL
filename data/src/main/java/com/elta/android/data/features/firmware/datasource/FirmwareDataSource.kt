package com.elta.android.data.features.firmware.datasource

import com.elta.android.data.features.firmware.dto.FirmwareDto
import io.reactivex.Single
import java.io.File

interface FirmwareDataSource {

    fun getFirmwareInfo(): Single<FirmwareDto>

    fun getFirmware(version: String): Single<File>
}