package com.elta.android.data.features.devices.glucometer.refactor

import android.bluetooth.le.ScanResult
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import kotlinx.coroutines.flow.Flow

interface Manager {
    /**
     *
     * Поиск глюклометров в окружении для добавления
     *
     * */
    fun findDevices(): Flow<List<ScanResult>>


    /**
     *
     * Получение актуальной информации с глюкометра
     *
     * */
    suspend fun getGlucometerInfo(address: String, pin: String): GlucometerInfoDto


    /**
     *
     * Первое подключение к глюкометру
     *
     * */
    suspend fun connectDevice(address: String, pin: String)

    /**
     *
     * Синхронизация с глюкометром
     *
     * */
    suspend fun syncWithDevice(address: String, pin: String): List<GlucometerEventDto>

    /**
     *
     * Запускает сценарий поиска глюкометра
     *
     * */
    suspend fun findGlucometer(address: String, pin: String)


}
