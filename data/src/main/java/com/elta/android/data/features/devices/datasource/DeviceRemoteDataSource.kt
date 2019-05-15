package com.elta.android.data.features.devices.datasource

import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.glucometer.GlucometersManager
import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import no.nordicsemi.android.support.v18.scanner.ScanResult
import javax.inject.Inject

@Suppress("MagicNumber")
class DeviceRemoteDataSource @Inject constructor(
    private val scanToDtoMapper: Mapper<ScanResult, GlucometerDto>,
    private val glucometersManager: GlucometersManager
) : DeviceDataSource {

    override fun findDevices(): Observable<List<GlucometerDto>> =
        glucometersManager.findDevices().map(scanToDtoMapper::mapFromObjects)

    override fun getGlucometerInfo(address: String): Single<GlucometerInfoDto> =
        glucometersManager.getGlucometerInfo(address)

    override fun getLastGlucometerInfo(address: String): Single<GlucometerInfoDto> =
        glucometersManager.getLastGlucometerInfo(address)

    override fun getGlucometerEvents(address: String): Single<List<String>> =
        glucometersManager.getGlucometerEvents(address).map { it.map { it.toString() } }

    override fun connectDevice(device: GlucometerDto, pinCode: String): Completable =
        glucometersManager.connectDevice(device, pinCode)

    override fun syncWithDevice(device: GlucometerDto?): Single<List<GlucometerEventDto>> =
        glucometersManager.syncWithDevice(device)

    override fun updateFirmware(address: String, firmwareFile: FirmwareFile): Completable =
        glucometersManager.updateFirmware(address, firmwareFile)
}