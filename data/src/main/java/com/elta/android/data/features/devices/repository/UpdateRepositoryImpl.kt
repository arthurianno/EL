package com.elta.android.data.features.devices.repository

import com.elta.android.data.features.devices.glucometer.client.GlucometerClient
import com.elta.android.domain.features.devices.repository.UpdateRepository
import com.elta.android.domain.features.firmware.model.FirmwareFile
import javax.inject.Inject

class UpdateRepositoryImpl @Inject constructor(
    private val glucometerClient: GlucometerClient
) : UpdateRepository {

    override suspend fun updateFirmware(address: String, firmwareFile: FirmwareFile): String {
        return glucometerClient.updateFirmware(address, firmwareFile)
    }

}
