package com.elta.android.data.features.firmware.repository

import com.elta.android.common.di.qualifires.Cache
import com.elta.android.common.di.qualifires.Remote
import com.elta.android.common.errors.NoSuchFirmware
import com.elta.android.data.features.firmware.datasource.FirmwareDownloadDataSource
import com.elta.android.data.features.firmware.datasource.info.FirmwareInfoDataSource
import com.elta.android.data.features.firmware.toDomain
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.firmware.model.FirmwareFile
import com.elta.android.domain.features.firmware.model.FirmwareInfo
import com.elta.android.domain.features.firmware.repository.FirmwareRepository
import io.reactivex.Single
import javax.inject.Inject

class FirmwareDataRepository @Inject constructor(
    @Remote private val remoteSource: FirmwareDownloadDataSource,
    @Cache private val localSource: FirmwareDownloadDataSource,
    @Remote private val remoteInfoSource: FirmwareInfoDataSource,
) : FirmwareRepository {

    override fun getFirmwareInfo(
        glucometerInfo: GlucometerInfo
    ): Single<FirmwareInfo> =
        remoteInfoSource.getFirmwareInfo(
            glucometerInfo = glucometerInfo
        )
            .map { it.toDomain() }

    override fun downloadFirmware(firmwareInfo: FirmwareInfo): Single<FirmwareFile> =
        localSource.downloadFirmware(firmwareInfo)
            .onErrorResumeNext { error ->
                when (error) {
                    is NoSuchFirmware -> remoteSource.downloadFirmware(firmwareInfo)
                    else -> Single.error(error)
                }
            }
            .map { it.toDomain() }
}
