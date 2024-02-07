package com.elta.android.data.features.devices.repository

import android.bluetooth.le.ScanResult
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.glucometer.builder.GlucometerEventBuilder
import com.elta.android.data.features.devices.glucometer.client.GlucometerClient
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.devices.repository.DeviceRepository
import com.elta.android.domain.features.rostech.RosTechRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DeviceDataRepository @Inject constructor(
    private val glucometerClient: GlucometerClient,
    private val scanToDtoMapper: Mapper<ScanResult, GlucometerDto>,
    private val glucometerToDomainMapper: Mapper<GlucometerDto, Glucometer>,
    private val glucometerInfoToDomainMapper: Mapper<GlucometerInfoDto, GlucometerInfo>,
    private val glucometerEventBuilder: GlucometerEventBuilder,
    //TODO: не самое удобное место, но работу с SDK все равно нужно переделывать
    private val rosTechRepository: RosTechRepository
) : DeviceRepository {

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
    override suspend fun syncWithDevice(
        address: String,
        email: String,
        serial: String?,
        lastSyncEvent: String?,
        onCommandSuccess: () -> Unit
    ): List<GlucometerEvent> =
        glucometerClient.syncWithDevice(address, lastSyncEvent, onCommandSuccess)
            .also { rosTechRepository.sendEvents(address, it) }
            .map { event -> glucometerEventBuilder.buildFrom(email, address, event, serial) }

    override suspend fun locateGlucometer() {
        glucometerClient.locateGlucometer()
    }

    override suspend fun testAllDevice(address: String, pinCode: String) {
        glucometerClient.testAllCommands(address, pinCode)
    }
}
