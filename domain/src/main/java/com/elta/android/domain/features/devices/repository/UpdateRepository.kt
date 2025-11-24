package com.elta.android.domain.features.devices.repository

import com.elta.android.domain.features.firmware.model.FirmwareFile


interface UpdateRepository {
    /**
     * Обновление прошивки глюкометра на новую версию. Через этот метод обнавляются прошивки до версии 4.4.9.
     * @param address Адрес запрашиваемого устройства.
     * @param firmwareFile Класс имеющий данные о версии новой прошивки и пути к ней в файловой системе.
     * @return Возвращает строку-результат для обновления устройства.
     */
    suspend fun updateFirmwareWithDfuMode(address: String, firmwareFile: FirmwareFile): String

    /**
     * Обновление прошивки глюкометра на новую версию. Через этот метод обнавляются прошивки от версии 4.5.0 и выше
     * @param address Адрес запрашиваемого устройства.
     * @param pin Пин-код устройства.
     * @param firmwareFile Класс имеющий данные о версии новой прошивки и пути к ней в файловой системе.
     */
    suspend fun updateFirmwareWithBootMode(address: String, pin: String, firmwareFile: FirmwareFile)
}
