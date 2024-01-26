package com.elta.android.data.features.devices.repository

import android.bluetooth.le.ScanResult
import com.elta.android.common.mapper.Mapper
import com.elta.android.common.repository.BaseRepository
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.glucometer.client.GlucometerClient
import com.elta.android.data.features.devices.glucometer.service.GlucometersService
import com.elta.android.data.features.devices.mapper.toDomain
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.devices.repository.DeviceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DeviceDataRepository @Inject constructor(
    private val glucometersService: GlucometersService, //TODO: перевести на manager
    private val glucometerClient: GlucometerClient,
    private val scanToDtoMapper: Mapper<ScanResult, GlucometerDto>,
    private val glucometerToDomainMapper: Mapper<GlucometerDto, Glucometer>,
    private val glucometerInfoToDomainMapper: Mapper<GlucometerInfoDto, GlucometerInfo>,
    override val dispatcher: CoroutineDispatcher
) : DeviceRepository, BaseRepository {

    override fun findDevices(): Flow<List<Glucometer>> =
        glucometerClient.findDevices()
            .map(scanToDtoMapper::mapFromObjects)
            .map(glucometerToDomainMapper::mapFromObjects)

    override suspend fun connectDevice(address: String, pinCode: String) =
        glucometerClient.connectDevice(address, pinCode)

    override suspend fun disconnect() {
        glucometerClient.disconnect()
    }

    override suspend fun getGlucometerInfo(address: String): GlucometerInfo {
        val info = glucometerClient.getGlucometerInfo(address)
        return glucometerInfoToDomainMapper.mapFromObject(info)
    }

    //TODO: зачем тут Int, если надо GlucometerEventDto
    override suspend fun syncWithDevice(address: String, email: String, serial: String?, lastSyncEvent: String?): List<GlucometerEvent> =
        glucometerClient.syncWithDevice(address, email, serial, lastSyncEvent)
            .map { glucometerEventDto ->
                glucometerEventDto.toDomain()
            }

    override fun findGlucometer(address: String): Flow<Unit> =
        glucometersService.findGlucometer(address)
            .flowOn(dispatcher)

    override suspend fun testAllDevice(address: String, pinCode: String) {
        glucometerClient.testAllCommands(address, pinCode)
    }
}
