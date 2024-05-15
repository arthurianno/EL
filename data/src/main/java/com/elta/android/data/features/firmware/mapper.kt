package com.elta.android.data.features.firmware // ktlint-disable filename

import com.elta.android.data.features.firmware.model.FirmwareFileStorageEntity
import com.elta.android.data.features.firmware.model.NewVersionFirmwareInfoResponse
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.elta.android.domain.features.firmware.model.FirmwareInfo
import com.elta.android.domain.features.firmware.model.FirmwareMode
import org.greenrobot.essentials.io.IoUtils
import java.io.File
import java.io.FileInputStream

internal fun FirmwareInfo.toFirmwareFileStorage(file: File): FirmwareFileStorageEntity =
    FirmwareFileStorageEntity(
        version = version,
        path = file.absolutePath,
        hash = IoUtils.getMd5(FileInputStream(file))
    )

internal fun FirmwareFileStorageEntity.toDomain(): FirmwareFile =
    FirmwareFile(
        version = version,
        path = path,
        hash = hash
    )


internal fun NewVersionFirmwareInfoResponse.toDomain(): FirmwareInfo =
    FirmwareInfo(
        id = id,
        version = version,
        size = size,
        hash = hash,
        firmwareMode = FirmwareMode.toFirmwareMode(dfuMode)
    )
