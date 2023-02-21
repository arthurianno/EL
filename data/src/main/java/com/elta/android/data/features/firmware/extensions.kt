package com.elta.android.data.features.firmware // ktlint-disable filename

import com.elta.android.data.features.firmware.model.FirmwareFileStorageEntity
import com.elta.android.domain.features.firmware.model.Firmware
import io.reactivex.Single

internal fun Single<FirmwareFileStorageEntity>.validateFileHash(
    firmware: Firmware,
    error: Throwable
): Single<FirmwareFileStorageEntity> =
    compose { files ->
        files.flatMap { file ->
            if (file.hash.equals(firmware.hash, true)) {
                Single.just(file)
            } else {
                Single.error(error)
            }
        }
    }
