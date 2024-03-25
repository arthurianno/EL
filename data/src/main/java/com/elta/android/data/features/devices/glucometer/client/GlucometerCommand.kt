package com.elta.android.data.features.devices.glucometer.client

import android.bluetooth.BluetoothDevice
import com.elta.android.data.features.devices.dto.VersionDto
import org.threeten.bp.ZonedDateTime

interface GlucometerCommand {

    /**
     * Метод для подключения к глюкометру
     * @param device - Блютус устройство которое получается как результат сканирования
     */
    suspend fun connectToGlucometer(device: BluetoothDevice)

    /**
     * Метод для отключения от глюкометра
     */
    suspend fun disconnectGlucometer()

    /**
     * Метод проверки пин-кода на устройстве
     * @return признак того что проверка пина прашла успешно
     */
    suspend fun checkPin(pin: String): Boolean

    /**
     * Переводит устройство в режим прошивки
     * @return строка с результатом выполнения комманды
     */
    suspend fun toDfuMode(): String

    /**
     * получение даты и времени с глюкометра
     * @return дата и время
     */
    suspend fun getDate(): ZonedDateTime?

    /**
     * Получение версий устройства
     * @return модель с версиям софта и железа
     */
    suspend fun getVersion(): VersionDto

    /**
     * Получение уровня батареи и температуры
     * @return пара уровень батареи к температуре
     */
    suspend fun getBatteryAndTemperature(): Pair<Int, Int>

    /**
     * Включение режима антипотеряшка
     * @return результат выполнения комманды в строковом виде комманды
     */
    suspend fun turnOnFindMode(): String

    /**
     * обновление даты и времени на устройстве
     * @param date новая дата и время
     * @return строка с результатом выполнения комманды
     */
    suspend fun updateTime(date: ZonedDateTime): String

    /**
     * считывание замера с устройства
     * @return строковое представление замера с устройства
     */
    suspend fun readEvent(index: Int): String

    /**
     * Получение серийного номер устройства
     * @return серийный номер
     */
    suspend fun getSerialNumber(): String
}
