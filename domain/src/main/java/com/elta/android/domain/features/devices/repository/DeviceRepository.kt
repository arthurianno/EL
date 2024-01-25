package com.elta.android.domain.features.devices.repository

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions", "ComplexInterface")
interface DeviceRepository {

    /**
     * Поиск устройств в окружении.
     * @return Возвращает преобразованный список устройств .
     */
    fun findDevices(): Flow<List<Glucometer>>

    /**
     * Получение информации об устройстве.
     * @param address Адрес запрашиваемого устройства.
     * @param pinCode Пин-код для подключения к устройству.
     * @return Возвращает информацию об устройстве: заряд батареи, версия прошивки и тд.
     */
    suspend fun getGlucometerInfo(address: String, pinCode: String): GlucometerInfo

    /**
     * Подключение к устройству.
     * @param address Адрес запрашиваемого устройства.
     * @param pinCode Пин-код для подключения к устройству.
     */
    suspend fun connectDevice(address: String, pinCode: String)

    /**
     * Синхронизация с устройством для получения всех сохраненных событий на устройстве.
     * @param address Адрес запрашиваемого устройства.
     * @param pinCode Пин-код для подключения к устройству.
     * @param email Электронная почта пользователя, которая преоразуется в пользовательский идентификатор.
     * Он необходим для сохранения в БД и отправления на сервер.
     * @return Список преоразованных событий с устройства.
     */
    suspend fun syncWithDevice(address: String, pinCode: String, email: String): List<GlucometerEvent>

    /**
     * Обновление прошивки глюкометра на новую версию.
     * @param address Адрес запрашиваемого устройства.
     * @param firmwareFile Класс имеющий данные о версии новой прошивки и пути к ней в файловой системе.
     * @return Возвращает строку-команду для обновления устройства.
     */
    fun updateFirmware(address: String, firmwareFile: FirmwareFile): Observable<String>

    /**
     * Метод для обнаружение устройства. В случае обнаружения, отправляется команда к глюкометру.
     * Устройство будет издавать звук после команды.
     * @param address Адрес запрашиваемого устройства.
     * @return Возвращает пустой флоу.
     */
    fun findGlucometer(address: String): Flow<Unit>

    /**
     * @suppress Метод для тестирования
     * @param address Адрес запрашиваемого устройства.
     * @param pinCode Пин-код для подключения к устройству.
     */
    suspend fun testAllDevice(address: String, pinCode: String)
}
