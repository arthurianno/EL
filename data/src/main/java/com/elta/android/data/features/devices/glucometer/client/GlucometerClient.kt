package com.elta.android.data.features.devices.glucometer.client

import android.bluetooth.le.ScanResult
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import kotlinx.coroutines.flow.Flow

interface GlucometerClient {
    /**
     *
     * Поиск глюклометров в окружении для добавления
     * @return Flow которые порождает списки с результами сканирования окружения
     *
     * */
    fun findDevices(): Flow<List<ScanResult>>


    /**
     *
     * Подключение к глюкометру
     * @param address - мак-адрес устройства
     * @param pin - пин-код для подключения к устройству
     * */
    suspend fun connectDevice(address: String, pin: String)

    /**
     *
     * Отключение от глюкометра
     * */
    suspend fun disconnect()


    /**
     *
     * Получение актуальной информации с глюкометра
     *
     * */
    suspend fun getGlucometerInfo(address: String): GlucometerInfoDto

    //TODO: надо оставить просто чтение событий
    /**
     *
     * Синхронизация с глюкометром
     *
     * */
    suspend fun syncWithDevice(address: String, lastSyncEvent: String?): List<String>

    /**
     *
     * Запускает сценарий поиска глюкометра
     *
     * */
    suspend fun findGlucometer(address: String, pin: String)


    suspend fun testAllCommands(address: String, pin: String)


}
