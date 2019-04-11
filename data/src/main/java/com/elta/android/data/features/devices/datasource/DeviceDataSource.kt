package com.elta.android.data.features.devices.datasource

import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DeviceDataSource {

    fun findDevices(): Observable<List<GlucometerDto>>

    fun getGlucometerInfo(address: String): Single<GlucometerInfoDto>

    fun getGlucometerEvents(address: String): Single<List<String>>

    fun setPinCode(address: String, pinCode: String): Completable
}