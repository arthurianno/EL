package com.elta.android.data.features.devices.repository

import android.bluetooth.le.ScanResult
import com.elta.android.common.di.qualifires.Firmware
import com.elta.android.common.di.qualifires.UpdateType
import com.elta.android.common.mapper.Mapper
import com.elta.android.data.features.devices.dto.GlucometerDto
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.glucometer.builder.GlucometerEventBuilder
import com.elta.android.data.features.devices.glucometer.client.GlucometerClient
import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.devices.model.GlucometerEvent
import com.elta.android.domain.features.devices.model.GlucometerInfo
import com.elta.android.domain.features.devices.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DeviceDataRepository @Inject constructor(
    @Firmware(UpdateType.NordicDfu) private val glucometerClient: GlucometerClient,
    private val scanToDtoMapper: Mapper<ScanResult, GlucometerDto>,
    private val glucometerToDomainMapper: Mapper<GlucometerDto, Glucometer>,
    private val glucometerInfoToDomainMapper: Mapper<GlucometerInfoDto, GlucometerInfo>,
    private val glucometerEventBuilder: GlucometerEventBuilder
) : DeviceRepository {

    override fun findDevices(): Flow<List<Glucometer>> =
        glucometerClient.findDevices()
            .map(scanToDtoMapper::mapFromObjects)
            .map(glucometerToDomainMapper::mapFromObjects)

    override suspend fun connectDevice(address: String, pinCode: String) =
        glucometerClient.connectDevice(address, pinCode)

    override suspend fun connectDeviceDirectly(address: String, pinCode: String) =
        glucometerClient.connectDeviceDirectly(address, pinCode)

    override suspend fun disconnect() {
        glucometerClient.disconnect()
    }

    override suspend fun getVersions(address: String): Pair<String?, String?> {
        val hardwareToSoftware = glucometerClient.getVersions(address)
        return hardwareToSoftware.hardware to hardwareToSoftware.software
    }

    override suspend fun getGlucometerInfo(address: String): GlucometerInfo {
        val info = glucometerClient.getGlucometerInfo(address)
        return glucometerInfoToDomainMapper.mapFromObject(info)
    }
    override suspend fun syncWithDevice(
        address: String,
        lastSyncEvent: String?,
        onCommandSuccess: () -> Unit
    ): List<String> {
        return glucometerClient.syncWithDevice(address, lastSyncEvent, onCommandSuccess)
    }

    override suspend fun locateGlucometer() {
        glucometerClient.locateGlucometer()
    }

    override suspend fun turnOnDfuMode() {
        glucometerClient.turnOnDfuMode()
    }

    override suspend fun testAllDevice(address: String, pinCode: String) {
        glucometerClient.testAllCommands(address, pinCode)
    }

    override suspend fun buildEvents(
        address: String,
        email: String,
        serial: String?,
        measurements: List<String>
    ): List<GlucometerEvent> {
        return measurements.map { event ->
            glucometerEventBuilder.buildFrom(
                email,
                address,
                event,
                serial
            )
        }
    }
}
