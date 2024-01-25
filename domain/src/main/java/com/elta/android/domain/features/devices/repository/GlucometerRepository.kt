package com.elta.android.domain.features.devices.repository

import com.elta.android.domain.features.devices.model.Glucometer

interface GlucometerRepository {

    /**
     * Получение главного устройства из БД.
     * @return Возвращает главное устройство.
     */
    fun getPrimaryDevice(): Glucometer?

    /**
     * Помещает все данные об устройстве в БД.
     * @param glucometer Устройство которое собираемся сохранить.
     * @param isPrimary Является ли оно основным.
     */
    fun putDevice(glucometer: Glucometer, isPrimary: Boolean)

}

//FIXME GlucometerRepository and DeviceInfoRepisotory - объединить, потому что они оба ходят в БД за глюкометром.