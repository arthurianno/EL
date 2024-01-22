package com.elta.android.data.features.devices.repository

import android.bluetooth.le.ScanResult
import com.elta.android.common.mapper.Mapper
import com.elta.android.common.repository.BaseRepository
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.glucometer.refactor.Manager
import com.elta.android.data.features.devices.glucometer.service.GlucometersService
import com.elta.android.data.features.devices.glucometer.service.firmware.FirmwareService
import com.elta.android.data.features.devices.mapper.toDomain
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.firmware.model.FirmwareFile
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DeviceDataRepository @Inject constructor(
    private val glucometersService: GlucometersService, //TODO: перевести на manager
    private val firmwareService: FirmwareService, //TODO: перевести на manager

    private val manager: Manager, //FIXME: RENAME

    private val scanToDtoMapper: Mapper<ScanResult, GlucometerDto>,
    private val glucometerToDomainMapper: Mapper<GlucometerDto, Glucometer>,
    private val glucometerInfoToDomainMapper: Mapper<GlucometerInfoDto, GlucometerInfo>,
    override val dispatcher: CoroutineDispatcher
) : DeviceRepository, BaseRepository {

    override fun findDevices(): Flow<List<Glucometer>> =
        manager.findDevices()
            .map(scanToDtoMapper::mapFromObjects)
            .map(glucometerToDomainMapper::mapFromObjects)

    override suspend fun connectDevice(address: String, pinCode: String) =
        manager.connectDevice(address, pinCode)


    override suspend fun getGlucometerInfo(address: String, pinCode: String): GlucometerInfo {
        val info = manager.getGlucometerInfo(address, pinCode)
        return glucometerInfoToDomainMapper.mapFromObject(info)
    }

    //TODO: зачем тут Int, если надо GlucometerEventDto
    override suspend fun syncWithDevice(address: String, pinCode: String, email: String): List<GlucometerEvent> =
        manager.syncWithDevice(address, pinCode, email)
            .map { glucometerEventDto ->
                glucometerEventDto.toDomain()
            }


    override fun updateFirmware(address: String, firmwareFile: FirmwareFile): Observable<String> =
        firmwareService.updateFirmware(address, firmwareFile)


    override fun findGlucometer(address: String): Flow<Unit> =
        glucometersService.findGlucometer(address)
            .flowOn(dispatcher)

    override suspend fun testAllDevice(address: String, pinCode: String) {
        manager.testAllCommands(address, pinCode)
    }
}
