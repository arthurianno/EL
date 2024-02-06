package com.elta.android.domain.features.devices.repository

import com.elta.android.domain.features.firmware.model.FirmwareFile

@Suppress("TooManyFunctions", "ComplexInterface")
interface UpdateRepository {
    /**
     * Обновление прошивки глюкометра на новую версию.
     * @param address Адрес запрашиваемого устройства.
     * @param firmwareFile Класс имеющий данные о версии новой прошивки и пути к ней в файловой системе.
     * @return Возвращает строку-команду для обновления устройства.
     */
    suspend fun updateFirmware(address: String, firmwareFile: FirmwareFile): String
}
