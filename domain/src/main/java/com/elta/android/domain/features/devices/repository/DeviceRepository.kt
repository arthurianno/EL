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
     * @return Возвращает информацию об устройстве: заряд батареи, версия прошивки и тд.
     */
    suspend fun getGlucometerInfo(address: String): GlucometerInfo

    /**
     * Подключение к устройству.
     * @param address Адрес запрашиваемого устройства.
     * @param pinCode Пин-код для подключения к устройству.
     */
    suspend fun connectDevice(address: String, pinCode: String)

    /**
     * Отключение от текущего устройства
     */
    suspend fun disconnect()

    /**
     * Синхронизация с устройством для получения всех сохраненных событий на устройстве.
     * @param address Адрес запрашиваемого устройства.
     * @param email Электронная почта пользователя, которая преоразуется в пользовательский идентификатор.
     * @param serial Серийный номер устройства
     * @param lastSyncEvent Последний считайнный замер с устройства в строковом представлении
     * Он необходим для сохранения в БД и отправления на сервер.
     * @return Список преоразованных событий с устройства.
     */
    suspend fun syncWithDevice(address: String, email: String, serial: String?, lastSyncEvent: String?): List<GlucometerEvent>

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
