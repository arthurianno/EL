package com.elta.android.domain.features.devices.repository

import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Observable

@Suppress("TooManyFunctions", "ComplexInterface")
interface UpdateRepository {
    /**
     * Обновление прошивки глюкометра на новую версию.
     * @param address Адрес запрашиваемого устройства.
     * @param firmwareFile Класс имеющий данные о версии новой прошивки и пути к ней в файловой системе.
     * @return Возвращает строку-команду для обновления устройства.
     */
    fun updateFirmware(address: String, firmwareFile: FirmwareFile): Observable<String>
}
