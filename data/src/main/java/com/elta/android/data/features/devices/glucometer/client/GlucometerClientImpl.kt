package com.elta.android.data.features.devices.glucometer.client

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.elta.android.common.errors.GlucometerConnectionException
import com.elta.android.common.errors.GlucometerLowBatteryLevelError
import com.elta.android.common.errors.GlucometerNotConnectedException
import com.elta.android.common.errors.GlucometerPinIncorrect
import com.elta.android.common.errors.GlucometerToDfuModeError
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.glucometer.firmware.FirmwareManager
import com.elta.android.data.features.devices.glucometer.service.isEmptyEvent
import com.elta.android.data.features.devices.glucometer.service.isOk
import com.elta.android.domain.features.firmware.model.FirmwareFile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.runningFold
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class GlucometerClientImpl @Inject constructor(
    private val environmentScanner: EnvironmentScanner,
    private val glucometerBleManager: GlucometerBleManager,
    private val firmwareManager: FirmwareManager,
    private val crashlyticsReport: CrashlyticsReport
) : GlucometerClient {

    private val settings: ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
        .setReportDelay(0)
        .build()

    private val filters: List<ScanFilter> = listOf<ScanFilter>(
        ScanFilter.Builder()
            .setDeviceName("Satellite")
            .build()
    )

    private val dfuFilters: List<ScanFilter> = listOf<ScanFilter>(
        ScanFilter.Builder()
            .setDeviceName("Dfu")
            .build()
    )

    override suspend fun updateFirmware(
        address: String,
        firmwareFile: FirmwareFile
    ): String {
        return firmwareManager.updateFirmware(address, firmwareFile.path)
    }

    override fun findDevices(): Flow<List<ScanResult>> {
        return callbackFlow {
            environmentScanner.startScan(filters, settings) {
                trySend(it)
            }
            awaitClose {
                environmentScanner.stopScan()
            }
        }
            .runningFold(
                emptyList(),
                operation = { accumulator: List<ScanResult>, new: List<ScanResult> ->
                    (accumulator + new).distinctBy { it.device.address }
                })
    }

    @Throws(GlucometerNotConnectedException::class)
    override suspend fun getGlucometerInfo(address: String): GlucometerInfoDto {
        crashlyticsReport.log("start getting glucometer info with address $address")
        with(glucometerBleManager) {
            if (!glucometerBleManager.isConnected(address)) {
                val error = GlucometerNotConnectedException(address)
                crashlyticsReport.writeException(error)
                throw error
            }

            val time = ZonedDateTime.now()
            crashlyticsReport.log("updating time to time")
            updateTime(time)
            val date = getDate()
            crashlyticsReport.log("obtained date $date")
            val (battery, temperature) = getBatteryAndTemperature()
            crashlyticsReport.log(
                "obtained battery and temperature levels:\n battery: $battery, temperature: $temperature"
            )
            val version = getVersion()
            crashlyticsReport.log(
                "obtained versions:\n hardware: ${version.hardware}, software: ${version.software}"
            )
            val serial = getSerialNumber()
            crashlyticsReport.log("obtained serial")

            crashlyticsReport.log("glucometer info received successfully")

            return GlucometerInfoDto(
                id = address,
                deviceDate = date,
                syncDate = ZonedDateTime.now(),
                temperature = temperature,
                batteryLevel = battery,
                version = version,
                glucometerSerialNumber = serial
            )
        }
    }

    override suspend fun connectDevice(address: String, pin: String, isDfuMode: Boolean) {
        crashlyticsReport.log("start connection with device $address, isDfuMode: $isDfuMode")
        val scanFilters = if (isDfuMode) dfuFilters else filters

        crashlyticsReport.log("start scanning")
        val scanResult = scan(address, scanFilters)
        crashlyticsReport.log("scan finished with result")

        try {
            crashlyticsReport.log("establishing connection with glucometer")
            glucometerBleManager.connectToGlucometer(scanResult.device)
        } catch (e: Exception){
            val error = GlucometerConnectionException(address)
            crashlyticsReport.writeException(error)
            throw error
        }

        if (!isDfuMode) {
            checkPin(pin)
        }
    }

    private suspend fun checkPin(pin: String) {
        crashlyticsReport.log("checking pin")
        val pinIsValid = glucometerBleManager.checkPin(pin)
        if (!pinIsValid) {
            glucometerBleManager.disconnectGlucometer()
            crashlyticsReport.log("pin invalid")
            throw GlucometerPinIncorrect
        }
        crashlyticsReport.log("pin valid")
    }

    override suspend fun disconnect() {
        crashlyticsReport.log("start glucometer disconections")
        glucometerBleManager.disconnectGlucometer()
    }

    @Throws(GlucometerNotConnectedException::class)
    override suspend fun syncWithDevice(
        address: String,
        lastSyncEvent: String?,
        onCommandSuccess: () -> Unit
    ): List<String> {
        crashlyticsReport.log("started receiving measurements from the device")
        with(glucometerBleManager) {
            if (!isConnected(address)) {
                val error = GlucometerNotConnectedException(address)
                crashlyticsReport.writeException(error)
                throw error
            }

            val events = mutableListOf<String>()

            crashlyticsReport.log("started reading measurements")

            for (index in 0 until 1000) {
                val event = readEvent(index)
                onCommandSuccess.invoke()
                if (event.isEmptyEvent() || event == lastSyncEvent) break
                events.add(event)
            }

            crashlyticsReport.log("all measurements have been successfully read, events size: $events")

            return events
        }
    }

    override suspend fun locateGlucometer() {
        glucometerBleManager.turnOnFindMode()
    }

    private suspend fun scan(address: String, filters: List<ScanFilter>): ScanResult {
        return suspendCoroutine { continuation ->
            environmentScanner.startScan(filters = filters, settings = settings) { scanResults ->
                scanResults.firstOrNull { it.device.address == address }?.let { result ->
                    continuation.resume(result)
                    environmentScanner.stopScan()
                }
            }
        }
    }


    override suspend fun turnOnDfuMode() {
        crashlyticsReport.log("start switching to dfu mode")
        val batteryLevel = glucometerBleManager.getBatteryAndTemperature().first

        if (batteryLevel <= MIN_BATTERY_LEVEL) {
            val error = GlucometerLowBatteryLevelError(
                current = batteryLevel,
                required = MIN_BATTERY_LEVEL
            )
            crashlyticsReport.writeException(error)
            throw error
        }

        val toDfuModeResult = glucometerBleManager.toDfuMode()
        if (!toDfuModeResult.isOk()) {
            crashlyticsReport.writeException(GlucometerToDfuModeError)
            throw GlucometerToDfuModeError
        }
        crashlyticsReport.log("successfully switched to dfu mode")
    }

    override suspend fun testAllCommands(address: String, pin: String) {
        val scanResult = scan(address, filters)
        glucometerBleManager.connectToGlucometer(scanResult.device)
        glucometerBleManager.checkPin(pin)
        glucometerBleManager.getDate()
        glucometerBleManager.getVersion()
        glucometerBleManager.getBatteryAndTemperature()
        glucometerBleManager.turnOnFindMode()
        glucometerBleManager.updateTime(ZonedDateTime.now())
        glucometerBleManager.getSerialNumber()
        repeat(1) { index ->
            glucometerBleManager.readEvent(index)
        }
        glucometerBleManager.disconnectGlucometer()
    }

}

private const val MIN_BATTERY_LEVEL = 1