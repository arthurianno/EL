package com.elta.android.domain.features.devices.repository

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface DeviceRepository {

    fun findDevices(): Observable<List<Glucometer>>

    fun getDevices(): Observable<List<Glucometer>>

    fun deleteDevices(address: String): Completable

    fun getDeviceInfo(address: String): Single<GlucometerInfo>

    fun getDeviceEvents(address: String): Single<List<String>>

    fun setPinCode(address: String, pinCode: String): Completable

    fun updateFirmware(address: String, firmwareFile: FirmwareFile): Completable
}