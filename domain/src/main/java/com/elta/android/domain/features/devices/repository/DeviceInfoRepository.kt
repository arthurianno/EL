package com.elta.android.domain.features.devices.repository

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.devices.model.GlucometerInfo

@Suppress("TooManyFunctions", "ComplexInterface")
interface DeviceInfoRepository {

    /**
     * Метод для получения всех кешированных устройств
     * @return список всех устройств с которыми происходила синхронизация в виде основной информации
     * по глюкометру и данных о нем полученных из последней синхронизации
     */
    fun getDevices(): List<Pair<Glucometer, GlucometerInfo>>


    /**
     * Получение основных данных об устройстве из кеша по его мак адресу
     * @param address мак-адрес устройства
     * @return основная иноформация по глюкометру
     */
    fun getDevice(address: String): Glucometer?

    /**
     * Удаление устройства из кеша по его мак-адрес
     * @param address мак-адрес устройства
     */
    fun deleteDevice(address: String)

    /**
     * Получение данных последней синхронизации об устройстве по его мак-адресу
     * @param address мак-адрес устройства
     */
    fun getLastDeviceInfo(address: String): GlucometerInfo?

    /**
     * Установка основного устройства с которым будет взаимодействовать пользователь
     * @param address мак-адрес устройства
     */
    fun setPrimaryDevice(address: String)

    /**
     * Получение данных об основном устройстве пользователя
     * @return данные в виде основной информации
     * по глюкометру и данных о нем полученных из последней синхронизации
     */
    fun getPrimaryDevice(): Pair<Glucometer, GlucometerInfo>?

    /**
     * Обновление данных об устройстве пользователя
     * @param glucometerInfo основные данные по глюкометру
     * @param lastSyncedEvent последнее синхроноизованное событие
     */
    fun updateGlucometerInfo(glucometerInfo: GlucometerInfo, lastSyncedEvent: GlucometerEvent?)

}