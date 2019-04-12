package com.elta.android.domain.features.devices.repository

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DeviceRepository {

    fun findDevices(): Observable<List<Glucometer>>

    fun getDeviceInfo(address: String): Single<GlucometerInfo>

    fun getDeviceEvents(address: String): Single<List<String>>

    fun setPinCode(address: String, pinCode: String): Completable
}