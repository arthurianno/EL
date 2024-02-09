package com.elta.android.data.features.devices.glucometer.client

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.elta.android.common.errors.GlucometerConnectionException
import com.elta.android.common.errors.GlucometerLowBatteryLevelError
import com.elta.android.common.errors.GlucometerNotConnectedException
import com.elta.android.common.errors.GlucometerNotFoundInDfuMode
import com.elta.android.common.errors.GlucometerPinIncorrect
import com.elta.android.common.errors.GlucometerSyncError
import com.elta.android.common.errors.GlucometerToDfuModeError
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.glucometer.firmware.FirmwareManager
import com.elta.android.data.features.devices.glucometer.service.isEmptyEvent
import com.elta.android.data.features.devices.glucometer.service.isOk
import com.elta.android.domain.features.devices.CONNECT_TIMEOUT
import com.elta.android.domain.features.firmware.model.FirmwareFile
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.withTimeout
import org.threeten.bp.ZonedDateTime
import java.lang.IllegalStateException
import java.util.concurrent.TimeoutException
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
        val scanResult = try {
            withTimeout(CONNECT_TIMEOUT) {
                scan(address, dfuFilters)
            }
        } catch (e: TimeoutCancellationException) {
            val exception = GlucometerSyncError(TimeoutException("Device search dfu $address timed out"))
            crashlyticsReport.writeException(exception)
            throw exception
        }

        if (scanResult.device.address != address) {
            crashlyticsReport.writeException(GlucometerNotFoundInDfuMode)
            throw GlucometerNotFoundInDfuMode
        }
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
        crashlyticsReport.log("Started receiving information from the device with the address: $address")
        with(glucometerBleManager) {
            if (!glucometerBleManager.isConnected(address)) {
                val error = GlucometerNotConnectedException(address)
                crashlyticsReport.writeException(error)
                throw error
            }

            val time = ZonedDateTime.now()
            crashlyticsReport.log("Update device time to $time")
            updateTime(time)
            val date = getDate()
            crashlyticsReport.log("Received device date and time = $date")
            val (battery, temperature) = getBatteryAndTemperature()
            crashlyticsReport.log(
                "Device temperature and battery levels obtained:\n battery: $battery, temperature: $temperature"
            )
            val version = getVersion()
            crashlyticsReport.log(
                "Device versions received:\n hardware: ${version.hardware}, software: ${version.software}"
            )
            val serial = getSerialNumber()
            crashlyticsReport.log("Serial number received")

            crashlyticsReport.log("Device information was successfully read")

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

    override suspend fun connectDevice(address: String, pin: String) {
        crashlyticsReport.log("Connection operations started with device $address")
        crashlyticsReport.log("Environment scanning started")
        val scanResult = scan(address, filters)
        crashlyticsReport.log("Scanning the environment is completed with the result")

        try {
            crashlyticsReport.log("Establishing a connection with a device")
            glucometerBleManager.connectToGlucometer(scanResult.device)
        } catch (e: Exception){
            val error = GlucometerConnectionException(e.message.orEmpty())
            crashlyticsReport.writeException(error)
            throw error
        }

        checkPin(pin)
    }

    private suspend fun checkPin(pin: String) {
        crashlyticsReport.log("Checking pin")
        val pinIsValid = glucometerBleManager.checkPin(pin)
        if (!pinIsValid) {
            glucometerBleManager.disconnectGlucometer()
            crashlyticsReport.log("Pin invalid")
            throw GlucometerPinIncorrect
        }
        crashlyticsReport.log("Pin valid")
    }

    override suspend fun disconnect() {
        crashlyticsReport.log("Started disconnecting the connection to the device")
        glucometerBleManager.disconnectGlucometer()
    }

    @Throws(GlucometerNotConnectedException::class)
    override suspend fun syncWithDevice(
        address: String,
        lastSyncEvent: String?,
        onCommandSuccess: () -> Unit
    ): List<String> {
        crashlyticsReport.log("The operation to obtain measurements from the device has begun")
        with(glucometerBleManager) {
            if (!isConnected(address)) {
                val error = GlucometerNotConnectedException(address)
                crashlyticsReport.writeException(error)
                throw error
            }

            val events = mutableListOf<String>()

            crashlyticsReport.log("Started reading measurements from the device")

            for (index in 0 until 1000) {
                val event = readEvent(index)
                onCommandSuccess.invoke()
                if (event.isEmptyEvent() || event == lastSyncEvent) break
                events.add(event)
            }

            crashlyticsReport.log("All measurements were successfully read, events size: $events")

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
                    try {
                        continuation.resume(result)
                    } catch (e: IllegalStateException) {
                        crashlyticsReport.log("Already resumed scan")
                    } finally {
                        environmentScanner.stopScan()
                    }
                }
            }
        }
    }


    override suspend fun turnOnDfuMode() {
        crashlyticsReport.log("Switching the device to dfu mode")
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
        crashlyticsReport.log("The device has successfully switched to dfu mode")
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