package com.elta.android.domain.features.devices.repository

interface PinRepository {

    /**
     * Получить пин-код устройства из БД.
     * @param address Адрес запрашиваемого устройства.
     * @return Возвращается пин-код состоящий из трех цифр.
     */
    fun getPin(address: String): String?

    /**
     * Сохранить пин-код устройства в БД.
     * @param address Адрес сохраняемого устройства.
     * @param pin Пин-код устройства.
     */
    fun savePin(address: String, pin: String)

    /**
     * Удаляет пин-код устройства из БД.
     * @param address Адрес сохраняемого устройства.
     */
    fun clearPin(address: String)
}
