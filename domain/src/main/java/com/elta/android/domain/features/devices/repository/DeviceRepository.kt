package com.elta.android.domain.features.devices.repository

import com.elta.android.domain.features.devices.model.Command
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.diary.events.model.Event
import io.reactivex.Observable
import io.reactivex.Single

interface DeviceRepository {

    fun findDevices(): Observable<List<Glucometer>>

    fun getDeviceInfo(address: String, fields: List<Command>): Single<GlucometerInfo>

    fun getDeviceEvents(): Single<List<Event>>
}