package com.elta.android.data.features.devices.dto

import org.threeten.bp.ZonedDateTime

/**
 * Содержит последние известные на момент синхронизации данные об устройстве
 * @param id идентификатор устройства
 * @param deviceDate дата и время на глюкометре
 * @param syncDate дата и время на устройстве пользователя
 * @param temperature температура устройства
 * @param batteryLevel уровень заряда батареи устройства
 * @param version версия глюкометра, состоит из версий софта и железа
 * @param lastSyncedEvent Последнее сихронизированное событие
 * @param glucometerSerialNumber серийный номер глюкометра
 */
data class GlucometerInfoDto(
    val id: String,
    val deviceDate: ZonedDateTime? = null,
    val syncDate: ZonedDateTime? = null,
    val temperature: Int? = null,
    val batteryLevel: Int? = null,
    val version: VersionDto? = null,
    val lastSyncedEvent: String? = null,
    val glucometerSerialNumber: String? = null
)
