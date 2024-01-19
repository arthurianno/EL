package com.elta.android.domain.features.devices.repository

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions", "ComplexInterface")
interface DeviceRepository {

    fun findDevices(): Flow<List<Glucometer>>

    fun getDevices(): Single<List<Pair<Glucometer, GlucometerInfo>>>

    fun getDevice(address: String): Single<Glucometer>

    fun deleteDevice(address: String): Completable

    fun getDeviceInfo(address: String): Single<GlucometerInfo>

    fun getLastDeviceInfo(address: String): Single<GlucometerInfo>

    suspend fun connectDevice(address: String, pinCode: String)

    fun syncWithDevice(device: Glucometer?): Observable<Int>

    fun updateFirmware(address: String, firmwareFile: FirmwareFile): Observable<String>

    fun setPrimaryDevice(address: String): Completable

    fun findGlucometer(address: String): Flow<Unit>

    suspend fun testAllDevice(address: String, pinCode: String)
}
