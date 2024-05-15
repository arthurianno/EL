package com.elta.android.data.features.devices.glucometer.firmware

interface FirmwareManager {

    /**
     * Обновление прошивки глюкометра для версий ниже 4.4.9.
     * @param address Mac-адрес глюкометра.
     * @param filePath содержит путь до файла обновления.
     * @return Результат обновления
     **/
    suspend fun updateFirmwareWithNordicDfu(address: String, filePath: String): String

    /**
     * Обновление прошивки глюкометра для версий выше 4.5.0.
     * @param address Mac-адрес глюкометра.
     * @param pin Пин-код устройства.
     * @param filePath содержит путь до файла обновления.
     **/
    suspend fun updateFirmwareWithBootMode(address: String, pin: String, filePath: String)
}
