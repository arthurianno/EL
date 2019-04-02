package com.elta.android.data.features.devices.datasource

import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.domain.features.devices.model.Command
import com.elta.android.domain.features.devices.model.GlucometerInfo
import io.reactivex.Observable
import io.reactivex.Single

interface DeviceDataSource {

    fun findDevices(): Observable<List<GlucometerDto>>

    fun getGlucometerInfo(address: String, commands: List<Command>): Single<GlucometerInfo>
}