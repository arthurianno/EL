package com.elta.android.domain.features.devices.repository

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.devices.model.GlucometerInfo
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions", "ComplexInterface")
interface DeviceRepository {

    /**
     * Поиск устройств в окружении.
     * @return Возвращает преобразованный список устройств .
     */
    fun findDevices(): Flow<List<Glucometer>>

    /**
     * Получении версий железа и софта устройства.
     * @param address Адрес запрашиваемого устройства.
     * @return Возвращает информацию об устройстве: заряд батареи, версия прошивки и тд.
     */
    suspend fun getVersions(address: String): Pair<String?, String?>

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
     * @param lastSyncEvent Последний считайнный замер с устройства в строковом представлении
     * @param onCommandSuccess коллбек вызывающися после каждой успешной выполненной комманды глюкометра,
     * Он необходим для сохранения в БД и отправления на сервер.
     * @return Список замеров с устройства.
     */
    suspend fun syncWithDevice(
        address: String,
        lastSyncEvent: String?,
        onCommandSuccess: () -> Unit
    ): List<String>

    /**
     * Метод для обнаружение устройства.
     * Устройство будет издавать звук после команды.
     */
    suspend fun locateGlucometer()

    /**
     * Перевод устройства в режим Dfu.
     */
    suspend fun turnOnDfuMode()

    /**
     * @suppress Метод для тестирования
     * @param address Адрес запрашиваемого устройства.
     * @param pinCode Пин-код для подключения к устройству.
     */
    suspend fun testAllDevice(address: String, pinCode: String)

    /**
     * @suppress Генерирует события из замеров
     * @param address Адрес запрашиваемого устройства.
     * @param email Email адрес пользователя.
     * @param serial Серийный номер устройства.
     * @param measurements список замеров которые превращаются в события.
     */
    suspend fun buildEvents(address: String, email: String, serial: String?, measurements: List<String>): List<GlucometerEvent>
}
