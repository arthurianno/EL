package com.elta.android.data.features.firmware.repository

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.firmware.datasource.FirmwareDataSource
import com.elta.android.data.features.firmware.dto.FirmwareDto
import com.elta.android.domain.features.firmware.model.Firmware
import com.elta.android.domain.features.firmware.repository.FirmwareRepository
import io.reactivex.Single
import java.io.File
import javax.inject.Inject

class FirmwareDataRepository @Inject constructor(
    private val toDomainMapper: Mapper<FirmwareDto, Firmware>,
    private val source: FirmwareDataSource
) : FirmwareRepository {

    override fun getFirmwareInfo(): Single<Firmware> =
        source.getFirmwareInfo().map(toDomainMapper::mapFromObject)

    override fun getFirmware(version: String): Single<File> =
        source.getFirmware(version)
}