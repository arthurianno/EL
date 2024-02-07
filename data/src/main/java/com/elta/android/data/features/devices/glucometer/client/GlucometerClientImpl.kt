package com.elta.android.data.features.devices.glucometer.client

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.elta.android.common.errors.GlucometerConnectionException
import com.elta.android.common.errors.GlucometerLowBatteryLevelError
import com.elta.android.common.errors.GlucometerNotConnectedException
import com.elta.android.common.errors.GlucometerPinIncorrect
import com.elta.android.common.errors.GlucometerToDfuModeError
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.glucometer.firmware.FirmwareManager
import com.elta.android.data.features.devices.glucometer.firmware.utils.toDfuAddress
import com.elta.android.data.features.devices.glucometer.service.isEmptyEvent
import com.elta.android.data.features.devices.glucometer.service.isOk
import com.elta.android.domain.features.firmware.model.FirmwareFile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.runningFold
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class GlucometerClientImpl @Inject constructor(
    private val environmentScanner: EnvironmentScanner,
    private val glucometerBleManager: GlucometerBleManager,
    private val firmwareManager: FirmwareManager,
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
        pin: String,
        firmwareFile: FirmwareFile
    ): String {
        setupDfuMode(address, pin)

        val dfuAddress = address.toDfuAddress()
        val scanDfu = scan(dfuAddress, dfuFilters)
        glucometerBleManager.connectToGlucometer(scanDfu.device)
        val updateResult = try {
            firmwareManager.updateFirmware(address, firmwareFile.path)
        } catch (e: Exception) {
            //Todo: в логи
            throw e
        } finally {
            disconnect()
        }
        return updateResult
    }

    override fun findDevices(): Flow<List<ScanResult>> {
        Timber.tag(TAG).d("Start find devices")
        return callbackFlow {
            environmentScanner.startScan(filters, settings) {
                Timber.tag(TAG).d("ScanResult :: $it")
                trySend(it) //TODO: в сценарии первого подключения, когда пользователь выбирает из разных
                // - необходимо остановить поиск после выбора нужного пользователю устройства.
            }
            awaitClose { environmentScanner.stopScan() }
        }
            .runningFold(
                emptyList(),
                operation = { accumulator: List<ScanResult>, new: List<ScanResult> ->
                    (accumulator + new).distinctBy { it.device.address }
                })
    }

    @Throws(GlucometerNotConnectedException::class)
    override suspend fun getGlucometerInfo(address: String): GlucometerInfoDto {
        Timber.tag(TAG).d("Start get glucometer info")
        //в логи firebasse

        with(glucometerBleManager) {
            if (!glucometerBleManager.isConnected(address)) {
                //TODO: в лог
                throw GlucometerNotConnectedException(address)
            }

            updateTime(ZonedDateTime.now())
            val date = getDate()
            val (battery, temperature) = getBatteryAndTemperature()
            val version = getVersion()
            val serial = getSerialNumber()

            Timber.tag(TAG).d("End get glucometer info")
            //в логи firebasse

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
        Timber.tag(TAG).d("Start connect to glucometer")

        val scanResult = scan(address, filters)
        //TODO в логи результаты сканирования
        try {
            glucometerBleManager.connectToGlucometer(scanResult.device)
        } catch (e: Exception){
            throw GlucometerConnectionException(address)
        }

        val pinIsValid = glucometerBleManager.checkPin(pin)
        if (!pinIsValid) {
            glucometerBleManager.disconnectGlucometer()
            throw GlucometerPinIncorrect
        }
    }

    override suspend fun disconnect() {
        glucometerBleManager.disconnectGlucometer()
    }

    @Throws(GlucometerNotConnectedException::class)
    override suspend fun syncWithDevice(
        address: String,
        lastSyncEvent: String?,
        onCommandSuccess: () -> Unit
    ): List<String> {
        Timber.tag(TAG).d("Start sync with glucometer")

        with(glucometerBleManager) {
            if (!isConnected(address)) {
                Timber.tag(TAG).d("glucometer not connected")
                throw GlucometerNotConnectedException(address)
            }

            val events = mutableListOf<String>()

            Timber.tag(TAG).d("start read events from glucometer")

            for (index in 0 until 1000) {
                val event = readEvent(index)
                onCommandSuccess.invoke()
                if (event.isEmptyEvent() || event == lastSyncEvent) break
                events.add(event)
            }

            Timber.tag(TAG).d("all values read successfully")

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

    private suspend fun setupDfuMode(address: String, pin: String) {
        val scan = scan(address, filters)
        glucometerBleManager.connectToGlucometer(scan.device)
        try {
            val isPinValid = glucometerBleManager.checkPin(pin)
            if (!isPinValid) {
                //в логи
                throw GlucometerPinIncorrect
            }
            val batteryLevel = glucometerBleManager.getBatteryAndTemperature().first

            if (batteryLevel <= MIN_BATTERY_LEVEL) {
                //в логи
                throw GlucometerLowBatteryLevelError(
                    current = batteryLevel,
                    required = MIN_BATTERY_LEVEL
                )
            }

            val toDfuModeResult = glucometerBleManager.toDfuMode()
            if (!toDfuModeResult.isOk()) {
                //в логи
                throw GlucometerToDfuModeError
            }
        } finally {
            glucometerBleManager.disconnectGlucometer()
        }
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

private const val TAG = "GLUCOMETER_CLIENT"
private const val MIN_BATTERY_LEVEL = 1