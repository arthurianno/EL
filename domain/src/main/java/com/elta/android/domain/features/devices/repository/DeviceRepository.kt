package com.elta.android.domain.features.devices.repository

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

@Suppress("TooManyFunctions", "ComplexInterface")
interface DeviceRepository {

    fun findDevices(): Observable<List<Glucometer>>

    fun getDevices(): Single<List<Glucometer>>

    fun getDevice(address: String): Single<Glucometer>

    fun deleteDevice(address: String): Completable

    fun getDeviceInfo(address: String): Single<GlucometerInfo>

    fun getLastDeviceInfo(address: String): Single<GlucometerInfo>

    fun getDeviceEvents(address: String): Single<List<String>>

    fun connectDevice(device: Glucometer, pinCode: String): Completable

    fun syncWithDevice(device: Glucometer?): Completable

    fun updateFirmware(address: String, firmwareFile: FirmwareFile): Completable

    fun setPrimaryDevice(address: String): Completable
}