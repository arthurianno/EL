package com.elta.android.data.features.devices.datasource

import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions", "ComplexInterface")
interface DeviceDataSource {

    fun findDevices(): Observable<List<GlucometerDto>>

    fun getDevices(): Single<List<Pair<GlucometerDto, GlucometerInfoDto>>>

    fun getDevice(address: String): Single<GlucometerDto>

    fun deleteDevice(address: String): Completable

    fun getGlucometerInfo(address: String): Single<GlucometerInfoDto>

    fun getLastGlucometerInfo(address: String): Single<GlucometerInfoDto>

    fun connectDevice(device: GlucometerDto, pinCode: String): Completable

    fun syncWithDevice(device: GlucometerDto?): Observable<List<GlucometerEventDto>>

    fun updateFirmware(address: String, firmwareFile: FirmwareFile): Observable<String>

    fun setPrimaryDevice(address: String): Completable

    fun findGlucometer(address: String): Flow<Unit>
}
