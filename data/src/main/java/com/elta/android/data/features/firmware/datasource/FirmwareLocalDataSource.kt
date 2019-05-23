package com.elta.android.data.features.firmware.datasource

import com.elta.android.common.errors.NoSuchFirmare
import com.elta.android.data.features.firmware.dto.FirmwareDto
import com.elta.android.data.features.firmware.dto.FirmwareFileDto
import com.elta.android.domain.features.firmware.model.Firmware
import io.reactivex.Single
import org.greenrobot.essentials.io.IoUtils
import java.io.FileInputStream
import javax.inject.Inject

class FirmwareLocalDataSource @Inject constructor(
    private val firmwaresManager: FirmwaresManager
) : FirmwareDataSource {

    override fun getFirmwareInfo(): Single<FirmwareDto> {
        throw UnsupportedOperationException("${this.javaClass.simpleName} doesn't support getFirmwareInfo.")
    }

    override fun getFirmware(firmware: Firmware): Single<FirmwareFileDto> =
        Single.fromCallable {
            val file = firmwaresManager.getFile(firmware.version)
            if (file != null) {
                FirmwareFileDto(
                    version = firmware.version,
                    compatible = firmware.compatible,
                    path = file.absolutePath,
                    hash = IoUtils.getMd5(FileInputStream(file))
                )
            } else {
                throw NoSuchFirmare
            }
        }
}