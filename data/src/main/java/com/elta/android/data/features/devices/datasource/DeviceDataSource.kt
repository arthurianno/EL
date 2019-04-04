package com.elta.android.data.features.devices.datasource

import com.elta.android.data.features.devices.dto.GlucometerDto
import io.reactivex.Observable

interface DeviceDataSource {

    fun findDevices(): Observable<List<GlucometerDto>>
}