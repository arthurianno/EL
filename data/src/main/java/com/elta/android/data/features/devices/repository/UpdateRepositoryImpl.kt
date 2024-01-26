package com.elta.android.data.features.devices.repository

import com.elta.android.data.features.devices.glucometer.service.firmware.FirmwareService
import com.elta.android.domain.features.devices.repository.UpdateRepository
import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Observable
import javax.inject.Inject

class UpdateRepositoryImpl @Inject constructor(
    private val firmwareService: FirmwareService, //TODO: перевести на manager
) : UpdateRepository {

    override fun updateFirmware(address: String, firmwareFile: FirmwareFile): Observable<String> =
        firmwareService.updateFirmware(address, firmwareFile)

}
