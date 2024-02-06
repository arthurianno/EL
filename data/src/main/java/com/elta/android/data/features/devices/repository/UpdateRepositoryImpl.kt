package com.elta.android.data.features.devices.repository

import com.elta.android.common.errors.GlucometerPinNotFoundInternaly
import com.elta.android.data.features.devices.glucometer.client.GlucometerClient
import com.elta.android.data.features.devices.glucometer.storage.DbGlucometerPinStorage
import com.elta.android.domain.features.devices.repository.UpdateRepository
import com.elta.android.domain.features.firmware.model.FirmwareFile
import javax.inject.Inject

class UpdateRepositoryImpl @Inject constructor(
    private val glucometerClient: GlucometerClient,
    private val pinStorage: DbGlucometerPinStorage
) : UpdateRepository {

    override suspend fun updateFirmware(address: String, firmwareFile: FirmwareFile): String {
        val pin = pinStorage.getPin(address)
        if (pin == null) {
            //В логи
            throw GlucometerPinNotFoundInternaly
        }
        return glucometerClient.updateFirmware(address, pin, firmwareFile)
    }

}
