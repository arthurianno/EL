package com.elta.android.data.features.devices.repository

import com.elta.android.common.di.qualifires.Firmware
import com.elta.android.common.di.qualifires.UpdateType
import com.elta.android.data.features.devices.glucometer.client.GlucometerClient
import com.elta.android.data.features.devices.glucometer.firmware.FirmwareManager
import com.elta.android.domain.features.devices.repository.UpdateRepository
import com.elta.android.domain.features.firmware.model.FirmwareFile
import javax.inject.Inject

class UpdateRepositoryImpl @Inject constructor(
    @Firmware(UpdateType.NordicDfu) private val glucometerClient: GlucometerClient,
    private val updateManager: FirmwareManager
) : UpdateRepository {

    override suspend fun updateFirmwareWithDfuMode(
        address: String,
        firmwareFile: FirmwareFile
    ): String {
        return glucometerClient.updateFirmwareWithNordicDfu(address, firmwareFile)
    }

    override suspend fun updateFirmwareWithBootMode(
        address: String,
        pin: String,
        firmwareFile: FirmwareFile
    ) {
        updateManager.updateFirmwareWithBootMode(address, pin, firmwareFile.path)
    }

}
