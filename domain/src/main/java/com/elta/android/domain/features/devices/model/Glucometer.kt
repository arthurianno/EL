package com.elta.android.domain.features.devices.model

/**
 * Класс олицетворяющий глюкометр в системе
 * @param id - индентификатор глюкометра
 * @param address - мак-адрес устройства
 * @param name - имя устройства
 * @param isPrimary - является ли устройством по умолчанию (т.к активным устройством может быть только одно)
 */
data class Glucometer(
    val id: String,
    val address: String,
    val name: String?,
    val isPrimary: Boolean
)
