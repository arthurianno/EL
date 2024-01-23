package com.elta.android.domain.features.devices.repository

import com.elta.android.domain.features.devices.model.Glucometer

interface GlucometerRepository {

    fun getPrimaryDevice(): Glucometer?

    fun putDevice(glucometer: Glucometer, isPrimary: Boolean)

}

//FIXME GlucometerRepository and DeviceInfoRepisotory - объединить, потому что они оба ходят в БД за глюкометром.