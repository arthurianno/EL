package com.elta.android.data.features.devices.datasource

import android.bluetooth.le.ScanResult
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerEventDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.glucometer.manager.GlucometersInfoManager
import com.elta.android.data.features.devices.glucometer.service.firmware.FirmwareService
import com.elta.android.data.features.devices.glucometer.service.GlucometersService
import com.elta.android.data.features.devices.glucometer.service.info.InfoService
import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@Suppress("MagicNumber")
class DeviceRemoteDataSource @Inject constructor(
    private val scanToDtoMapper: Mapper<ScanResult, GlucometerDto>,
    private val glucometersInfoManager: GlucometersInfoManager,


    private val glucometersService: GlucometersService,
    private val firmwareService: FirmwareService,
    private val infoService: InfoService,
) : DeviceDataSource {

    override fun findDevices(): Observable<List<GlucometerDto>> =
        glucometersService.findDevices().map(scanToDtoMapper::mapFromObjects)

    override fun getDevices(): Single<List<Pair<GlucometerDto, GlucometerInfoDto>>> =
        glucometersInfoManager.getDevices()

    override fun getDevice(address: String): Single<GlucometerDto> =
        glucometersInfoManager.getDevice(address)

    override fun deleteDevice(address: String): Completable =
        glucometersInfoManager.deleteDevice(address)

    override fun getGlucometerInfo(address: String): Single<GlucometerInfoDto> =
        infoService.fetchGlucometerInfo(address)

    override fun getLastGlucometerInfo(address: String): Single<GlucometerInfoDto> =
        glucometersInfoManager.getLastGlucometerInfo(address)

    override fun connectDevice(device: GlucometerDto, pinCode: String): Completable =
        glucometersService.connectDevice(device, pinCode)

    override fun syncWithDevice(device: GlucometerDto?): Observable<List<GlucometerEventDto>> =
        glucometersService.syncWithDevice(device)

    override fun updateFirmware(address: String, firmwareFile: FirmwareFile): Observable<String> =
        firmwareService.updateFirmware(address, firmwareFile)

    override fun setPrimaryDevice(address: String): Completable =
        glucometersInfoManager.setPrimaryDevice(address)

    override fun findGlucometer(address: String): Flow<Unit> =
        glucometersService.findGlucometer(address)
}
