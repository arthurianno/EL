package com.elta.android.data.features.devices.glucometer.firmware

import com.elta.android.domain.features.firmware.model.FirmwareFile

interface FirmwareManager {

    /**
     * Обновление прошивки глюкометра.
     * @param address Mac-адрес глюкометра.
     * @param filePath содержит путь до файла обновления.
     * @return
     **/
    suspend fun updateFirmware(address: String, filePath: String): String
}
