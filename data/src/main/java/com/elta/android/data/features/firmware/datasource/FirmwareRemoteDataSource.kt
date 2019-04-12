package com.elta.android.data.features.firmware.datasource

import com.elta.android.data.features.firmware.api.FirmwareApi
import com.elta.android.data.features.firmware.dto.FirmwareDto
import io.reactivex.Single
import java.io.File
import javax.inject.Inject

class FirmwareRemoteDataSource @Inject constructor(
    private val fileManager: FileManager,
    private val api: FirmwareApi
) : FirmwareDataSource {

    override fun getFirmwareInfo(): Single<FirmwareDto> =
        api.getFirmwareInfo()

    override fun getFirmware(version: String): Single<File> =
        api.getFirmware(version).map { fileManager.writeToFile(version, it) }

}