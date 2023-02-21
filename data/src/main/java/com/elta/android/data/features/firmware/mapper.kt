package com.elta.android.data.features.firmware // ktlint-disable filename

import com.elta.android.data.features.firmware.model.FirmwareFileStorageEntity
import com.elta.android.data.features.firmware.model.FirmwareNetworkResponse
import com.elta.android.domain.features.firmware.model.Firmware
import com.elta.android.domain.features.firmware.model.FirmwareFile
import org.greenrobot.essentials.io.IoUtils
import java.io.File
import java.io.FileInputStream

internal fun Firmware.toFirmwareFileStorage(file: File): FirmwareFileStorageEntity =
    FirmwareFileStorageEntity(
        version = version,
        compatible = compatible,
        path = file.absolutePath,
        hash = IoUtils.getMd5(FileInputStream(file))
    )

internal fun FirmwareFileStorageEntity.toDomain(): FirmwareFile =
    FirmwareFile(
        version = version,
        compatible = compatible,
        path = path,
        hash = hash
    )

internal fun FirmwareNetworkResponse.toDomain(): Firmware =
    Firmware(
        version = actual.version,
        compatible = compatible.orEmpty(),
        hash = actual.hash
    )
