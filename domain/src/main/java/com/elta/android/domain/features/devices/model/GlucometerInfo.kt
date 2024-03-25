package com.elta.android.domain.features.devices.model

import org.threeten.bp.ZonedDateTime

/**
 * Содержит последние известные на момент синхронизации данные об устройстве
 * @param id идентификатор устройства
 * @param deviceDate дата и время на глюкометре
 * @param syncDate дата и время на устройстве пользователя
 * @param temperature температура устройства
 * @param batteryLevel уровень заряда батареи устройства
 * @param softwareVersion версия прошивки устройства
 * @param hardwareVersion версия железа устройства
 * @param glucometerSerialNumber серийный номер глюкометра
 */
data class GlucometerInfo(
    val id: String,
    val deviceDate: ZonedDateTime? = null,
    val syncDate: ZonedDateTime? = null,
    val temperature: Int? = null,
    val batteryLevel: Int? = null,
    val softwareVersion: String? = null,
    val hardwareVersion: String? = null,
    val glucometerSerialNumber: String? = null,
    val lastSyncEvent: String? = null
)
