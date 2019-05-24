package com.elta.android.data.features.firmware.datasource

import com.elta.android.data.features.firmware.dto.FirmwareFileDto
import com.elta.android.domain.features.firmware.model.Firmware
import io.reactivex.Single
import org.greenrobot.essentials.io.IoUtils
import java.io.File
import java.io.FileInputStream

fun Single<FirmwareFileDto>.validateFileHash(firmware: Firmware, error: Throwable): Single<FirmwareFileDto> =
    compose { files ->
        files.flatMap { file ->
            if (file.hash.equals(firmware.hash, true)) Single.just(file)
            else Single.error(error)
        }
    }

fun Firmware.toFirmwareFileDto(file: File): FirmwareFileDto =
    FirmwareFileDto(
        version = version,
        compatible = compatible,
        path = file.absolutePath,
        hash = IoUtils.getMd5(FileInputStream(file))
    )
