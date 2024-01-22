package com.elta.android.domain.features.devices.repository

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow

@Suppress("TooManyFunctions", "ComplexInterface")
interface DeviceRepository {

    fun findDevices(): Flow<List<Glucometer>>

    suspend fun getGlucometerInfo(address: String, pinCode: String): GlucometerInfo

    suspend fun connectDevice(address: String, pinCode: String)

    suspend fun syncWithDevice(address: String, pinCode: String, email: String): List<GlucometerEvent>

    fun updateFirmware(address: String, firmwareFile: FirmwareFile): Observable<String>

    fun findGlucometer(address: String): Flow<Unit>

    suspend fun testAllDevice(address: String, pinCode: String)
}
