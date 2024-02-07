package com.elta.android.data.features.devices.glucometer.client

import android.bluetooth.le.ScanResult
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.domain.features.firmware.model.FirmwareFile
import kotlinx.coroutines.flow.Flow

interface GlucometerClient {
    /**
     * Поиск глюклометров в окружении для добавления
     * @return Flow которые порождает списки с результами сканирования окружения
     * */
    fun findDevices(): Flow<List<ScanResult>>

    /**
     * Подключение к глюкометру
     * @param address - мак-адрес устройства
     * @param pin - пин-код для подключения к устройству
     * */
    suspend fun connectDevice(address: String, pin: String)

    /**
     * Отключение от глюкометра
     * */
    suspend fun disconnect()

    /**
     * Получение актуальной информации с глюкометра
     * */
    suspend fun getGlucometerInfo(address: String): GlucometerInfoDto

    /**
     * Синхронизация с глюкометром
     * */
    suspend fun syncWithDevice(address: String, lastSyncEvent: String?): List<String>

    /**
     * Запускает сценарий поиска глюкометра
     * */
    suspend fun locateGlucometer()

    // TODO дописать документацию
    /**
     * Обновление прошивки глюкометра.
     * @param address Mac-адрес глюкометра.
     * @param pin Пин-код к глюкометру.
     * @param file Данные о файле с новой прошивкой (содержит путь до файла и версию).
     * @return
    **/
    suspend fun updateFirmware(address: String, pin: String, firmwareFile: FirmwareFile): String

    suspend fun testAllCommands(address: String, pin: String)
}
