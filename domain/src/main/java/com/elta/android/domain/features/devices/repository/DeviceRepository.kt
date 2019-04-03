package com.elta.android.domain.features.devices.repository

import com.elta.android.domain.features.devices.model.Glucometer
import io.reactivex.Observable

interface DeviceRepository {

    fun findDevices(): Observable<List<Glucometer>>
}