package com.elta.android.data.features.devices.datasource

import android.bluetooth.le.ScanResult
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.glucometer.GlucometersManager
import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@Suppress("MagicNumber")
class DeviceRemoteDataSource @Inject constructor(
    private val scanToDtoMapper: Mapper<ScanResult, GlucometerDto>,
    private val glucometersManager: GlucometersManager
) : DeviceDataSource {

    override fun findDevices(): Observable<List<GlucometerDto>> =
        glucometersManager.findDevices().map(scanToDtoMapper::mapFromObjects)

    override fun getDevices(): Single<List<Pair<GlucometerDto, GlucometerInfoDto>>> =
        glucometersManager.getDevices()

    override fun getDevice(address: String): Single<GlucometerDto> =
        glucometersManager.getDevice(address)

    override fun deleteDevice(address: String): Completable =
        glucometersManager.deleteDevice(address)

    override fun getGlucometerInfo(address: String): Single<GlucometerInfoDto> =
        glucometersManager.getGlucometerInfo(address)

    override fun getLastGlucometerInfo(address: String): Single<GlucometerInfoDto> =
        glucometersManager.getLastGlucometerInfo(address)

    override fun getGlucometerEvents(address: String): Single<List<String>> =
        glucometersManager.getGlucometerEvents(address).map { it.map { it.toString() } }

    override fun connectDevice(device: GlucometerDto, pinCode: String): Completable =
        glucometersManager.connectDevice(device, pinCode)

    override fun syncWithDevice(device: GlucometerDto?): Observable<List<GlucometerEventDto>> =
        glucometersManager.syncWithDevice(device)

    override fun updateFirmware(address: String, firmwareFile: FirmwareFile): Observable<String> =
        glucometersManager.updateFirmware(address, firmwareFile)

    override fun setPrimaryDevice(address: String): Completable =
        glucometersManager.setPrimaryDevice(address)

    override fun findGlucometer(address: String): Flow<Unit> =
        glucometersManager.findGlucometer(address)
}
