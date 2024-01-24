package com.elta.android.data.features.devices.glucometer.refactor

import android.bluetooth.le.ScanResult
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import kotlinx.coroutines.flow.Flow

interface Manager {
    /**
     *
     * Поиск глюклометров в окружении для добавления
     * @return Flow которые порождает списки с результами сканирования окружения
     *
     * */
    fun findDevices(): Flow<List<ScanResult>>


    /**
     *
     * Первое подключение к глюкометру
     * @param address - мак-адрес устройства
     * @param pin - пин-код для подключения к устройству
     * */
    suspend fun connectDevice(address: String, pin: String)


    /**
     *
     * Получение актуальной информации с глюкометра
     *
     * */
    suspend fun getGlucometerInfo(address: String, pin: String): GlucometerInfoDto

    //TODO: надо оставить просто чтение событий
    /**
     *
     * Синхронизация с глюкометром
     *
     * */
    suspend fun syncWithDevice(address: String, pin: String, email: String): List<GlucometerEventDto>

    /**
     *
     * Запускает сценарий поиска глюкометра
     *
     * */
    suspend fun findGlucometer(address: String, pin: String)


    suspend fun testAllCommands(address: String, pin: String)


}
