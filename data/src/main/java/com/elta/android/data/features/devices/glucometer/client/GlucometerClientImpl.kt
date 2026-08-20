package com.elta.android.data.features.devices.glucometer.client

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import com.elta.android.common.errors.GlucometerDeviceHardwareError
import com.elta.android.common.errors.GlucometerConnectionException

import com.elta.android.common.errors.GlucometerLowBatteryLevelError
import com.elta.android.common.errors.GlucometerNotConnectedException
import com.elta.android.common.errors.GlucometerNotFoundInDfuMode
import com.elta.android.common.errors.GlucometerPinIncorrect
import com.elta.android.common.errors.GlucometerSyncError
import com.elta.android.common.errors.GlucometerToDfuModeError
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.common.utils.hideMac
import com.elta.android.data.features.devices.dto.GlucometerInfoDto
import com.elta.android.data.features.devices.dto.VersionDto
import com.elta.android.data.features.devices.glucometer.fromGlucometerDateTime
import com.elta.android.data.features.devices.glucometer.firmware.FirmwareManager
import com.elta.android.data.features.devices.glucometer.protocol.ProtocolCapabilitiesResolver
import com.elta.android.data.features.devices.glucometer.service.isEmptyEvent
import com.elta.android.data.features.devices.glucometer.service.isEmptyMemoryEvent
import com.elta.android.data.features.devices.glucometer.service.isOk
import com.elta.android.domain.features.devices.CONNECT_TIMEOUT
import com.elta.android.domain.features.firmware.model.FirmwareFile
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.math.BigInteger
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs

@Singleton
class GlucometerClientImpl @Inject constructor(
    private val glucometerBleManager: GlucometerBleManager,
    private val firmwareManager: FirmwareManager,
    private val environmentScanner: EnvironmentScanner,
    private val crashlyticsReport: CrashlyticsReport,
) : GlucometerClient {
    private var lastConnectedDeviceName: String? = null

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

    private val dfuFilters: List<ScanFilter> = emptyList()

    override suspend fun updateFirmwareWithNordicDfu(
        address: String,
        firmwareFile: FirmwareFile
    ): String {
        val candidateAddresses = buildCandidateDfuAddresses(address)
        val candidateDfuName = buildCandidateDfuName()
        crashlyticsReport.log(
            "Start scanning DFU device. Candidates by address: $candidateAddresses, by name: ${candidateDfuName ?: "<none>"}"
        )
        val scanResult = try {
            withTimeout(CONNECT_TIMEOUT) {
                scanDfu(candidateAddresses, candidateDfuName, dfuFilters)
            }
        } catch (e: TimeoutCancellationException) {
            val exception =
                GlucometerSyncError(
                    TimeoutException(
                        "Device search dfu ${address.hideMac()} timed out. " +
                                "Candidates: ${candidateAddresses.map { it.hideMac() }}"
                    )
                )
            crashlyticsReport.writeException(exception)
            throw exception
        }

        val foundAddress = scanResult.device.address.uppercase()
        val foundName = scanResult.device.name ?: scanResult.scanRecord?.deviceName
        val matchesByAddress = foundAddress in candidateAddresses
        val matchesByName = candidateDfuName?.equals(foundName, ignoreCase = true) == true

        if (!matchesByAddress && !matchesByName) {
            crashlyticsReport.writeException(GlucometerNotFoundInDfuMode)
            throw GlucometerNotFoundInDfuMode
        }
        return firmwareManager.updateFirmwareWithNordicDfu(foundAddress, firmwareFile.path)
    }

    override fun findDevices(): Flow<List<ScanResult>> {
        return callbackFlow {
            environmentScanner.startScan(
                filters = filters,
                settings = settings,
                resultCallback = {
                    trySend(it)
                },
                errorCallback = {

                })
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
    override suspend fun getVersions(address: String): VersionDto {
        checkIsConnected(address)

        return glucometerBleManager.getVersion()
    }

    @Throws(GlucometerNotConnectedException::class)
    override suspend fun getGlucometerInfo(address: String): GlucometerInfoDto {
        crashlyticsReport.log("Started receiving information from the device with the address: ${address.hideMac()}")
        with(glucometerBleManager) {
            checkIsConnected(address)

            val initialDeviceDate = runCatching { getDate() }.getOrNull()
            val time = ZonedDateTime.now()
            crashlyticsReport.log("Update device time to $time")
            updateTime(time)
            val date = getDate()
            crashlyticsReport.log("Received device date and time = $date")
            val isTimeOutOfSync = if (initialDeviceDate != null) {
                val deltaSeconds = kotlin.math.abs(initialDeviceDate.toEpochSecond() - time.toEpochSecond())
                Timber.d("⏰ Time sync check: initialDeviceDate=$initialDeviceDate, phoneTime=$time, deltaSeconds=$deltaSeconds, maxAllowed=$MAX_ALLOWED_TIME_DELTA_SECONDS")
                deltaSeconds > MAX_ALLOWED_TIME_DELTA_SECONDS
            } else {
                Timber.d("⏰ Time sync check: initialDeviceDate is null")
                false
            }
            Timber.d("⏰ Time sync result: isTimeOutOfSync=$isTimeOutOfSync")
            if (isTimeOutOfSync) {
                crashlyticsReport.log("Device time before sync differed from phone time by > $MAX_ALLOWED_TIME_DELTA_SECONDS seconds")
            }
            val (battery, temperature) = getBatteryAndTemperature()
            crashlyticsReport.log(
                "Device temperature and battery levels obtained:\n battery: $battery, temperature: $temperature"
            )
            val version = getVersion()
            crashlyticsReport.log(
                "Device versions received:\n hardware: ${version.hardware}, software: ${version.software}"
            )
            val capabilities = ProtocolCapabilitiesResolver.resolve(version, getConnectedDeviceName())
            if (capabilities.supportsGetError || GlucometerDebugConfig.MOCK_HARDWARE_ERROR) {
                runCatching {
                    val errorWord = if (GlucometerDebugConfig.MOCK_HARDWARE_ERROR) 0x00000001L else getError()
                    crashlyticsReport.log("Device error word received: $errorWord")
                    if ((errorWord and HARDWARE_ERROR_MASK) != 0L) {
                        crashlyticsReport.log("Device hardware error detected! errorWord: $errorWord")
                        throw GlucometerDeviceHardwareError
                    }
                }.onFailure {
                    if (it is GlucometerDeviceHardwareError) throw it
                    crashlyticsReport.log("Unable to get device error word: ${it.message.orEmpty()}")
                }
            }

            if (capabilities.supportsSetZone) {

                val currentOffsetSeconds = ZonedDateTime.now().offset.totalSeconds
                runCatching {
                    updateZoneOffset(currentOffsetSeconds)
                    val confirmedOffset = getZoneOffsetSeconds()
                    crashlyticsReport.log("Device timezone updated to $confirmedOffset seconds")
                }.onFailure {
                    crashlyticsReport.log("Unable to update timezone via setzone/getzone: ${it.message.orEmpty()}")
                }
            }
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
                glucometerSerialNumber = serial,
                isTimeOutOfSync = isTimeOutOfSync
            )
        }
    }

    override suspend fun connectDevice(address: String, pin: String) {
        if (GlucometerDebugConfig.MOCK_HARDWARE_ERROR) throw GlucometerDeviceHardwareError
        crashlyticsReport.log("Connection operations started with device ${address.hideMac()}")

        crashlyticsReport.log("Environment scanning started")
        val scanResult = scan(address, filters)
        lastConnectedDeviceName = scanResult.device.name ?: scanResult.scanRecord?.deviceName
        crashlyticsReport.log("Scanning the environment is completed with the result")

        try {
            // Решение для huawei/honor на Android 10. Эти устройства не успевают освободить ресурс и
            // синхронизация не проходит. Поэтому нужна исскустенная задержка между scan и connect
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) delay(1_000)

            crashlyticsReport.log("Establishing a connection with a device")
            glucometerBleManager.connectToGlucometer(scanResult.device)
        } catch (e: Exception) {
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
        if (GlucometerDebugConfig.MOCK_HARDWARE_ERROR) throw GlucometerDeviceHardwareError
        crashlyticsReport.log("The operation to obtain measurements from the device has begun")

        with(glucometerBleManager) {
            checkIsConnected(address)
            val version = getVersion()
            val capabilities = ProtocolCapabilitiesResolver.resolve(version, getConnectedDeviceName())

            val shouldTryGetMem = capabilities.supportsGetMem
            val events = if (shouldTryGetMem) {
                runCatching {
                    readEventsFromMemory(lastSyncEvent, onCommandSuccess)
                }.getOrElse { error ->
                    if (error is UnknownGetMemCommandException) {
                        crashlyticsReport.log("getmem command is not supported, fallback to rd")
                        readEventsFromRd(lastSyncEvent, onCommandSuccess)
                    } else {
                        throw error
                    }
                }
            } else {
                readEventsFromRd(lastSyncEvent, onCommandSuccess)
            }

            crashlyticsReport.log("All measurements were successfully read, events size: ${events.size}")
            Timber.d("📡 Total events read from glucometer: ${events.size}")

            return events
        }
    }

    override suspend fun locateGlucometer() {
        glucometerBleManager.turnOnFindMode()
    }

    private suspend fun readEventsFromRd(
        lastSyncEvent: String?,
        onCommandSuccess: () -> Unit
    ): List<String> {
        val events = mutableListOf<String>()
        crashlyticsReport.log("Started reading measurements from rd")
        for (index in 0 until MAX_GLUCOSE_EVENTS_COUNT) {
            val event = glucometerBleManager.readEvent(index)
            Timber.d("📡 Raw rd event [index=$index]: '$event' (length=${event.length})")
            onCommandSuccess.invoke()
            if (event.isEmptyEvent() || matchesLastSyncedMeasurement(event, lastSyncEvent)) break
            events.add(event)
        }
        return events
    }

    private suspend fun readEventsFromMemory(
        lastSyncEvent: String?,
        onCommandSuccess: () -> Unit
    ): List<String> {
        val events = mutableListOf<String>()
        crashlyticsReport.log("Started reading measurements from getmem")
        for (index in 0 until MAX_GLUCOSE_EVENTS_COUNT) {
            val event = glucometerBleManager.readMemoryEvent(index)
            Timber.d("📡 Raw mem event [index=$index]: '$event' (length=${event.length})")
            if (event.isUnknownCommand()) {
                throw UnknownGetMemCommandException
            }
            onCommandSuccess.invoke()
            if (event.isEmptyMemoryEvent() || matchesLastSyncedMeasurement(event, lastSyncEvent)) break
            events.add(event)
        }
        return events
    }

    private suspend fun scan(address: String, filters: List<ScanFilter>): ScanResult {
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { environmentScanner.stopScan() }

            environmentScanner.startScan(
                filters = filters,
                settings = settings,
                resultCallback = { scanResults ->
                    scanResults.firstOrNull {
                        it.device.address.equals(address, ignoreCase = true)
                    }?.let { result ->
                        try {
                            continuation.resume(result)
                        } catch (e: IllegalStateException) {
                            crashlyticsReport.log("Already resumed scan")
                        } finally {
                            environmentScanner.stopScan()
                        }
                    }
                }, errorCallback = {
                    try {
                        continuation.resumeWithException(it)
                    } finally {
                        environmentScanner.stopScan()
                    }

                }
            )

        }
    }

    private suspend fun scanDfu(
        candidateAddresses: Set<String>,
        candidateDfuName: String?,
        filters: List<ScanFilter>
    ): ScanResult {
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { environmentScanner.stopScan() }

            environmentScanner.startScan(
                filters = filters,
                settings = settings,
                resultCallback = { scanResults ->
                    scanResults.firstOrNull { result ->
                        val resultAddress = result.device.address.uppercase()
                        val resultName = result.device.name ?: result.scanRecord?.deviceName
                        val addressMatched = resultAddress in candidateAddresses
                        val nameMatched = when {
                            candidateDfuName != null -> resultName.equals(candidateDfuName, ignoreCase = true)
                            else -> resultName?.startsWith(DFU_NAME_PREFIX, ignoreCase = true) == true
                        }
                        addressMatched || nameMatched
                    }?.let { result ->
                        try {
                            continuation.resume(result)
                        } catch (e: IllegalStateException) {
                            crashlyticsReport.log("Already resumed scan")
                        } finally {
                            environmentScanner.stopScan()
                        }
                    }
                },
                errorCallback = {
                    try {
                        continuation.resumeWithException(it)
                    } finally {
                        environmentScanner.stopScan()
                    }
                }
            )
        }
    }

    private fun buildCandidateDfuAddresses(address: String): Set<String> {
        val normalized = address.uppercase()
        return linkedSetOf(normalized).apply {
            normalized.shiftLastMacByteBy(-1)?.let(::add)
            normalized.shiftLastMacByteBy(1)?.let(::add)
        }
    }

    private fun buildCandidateDfuName(): String? {
        val suffix = lastConnectedDeviceName
            ?.takeLast(DFU_NAME_SUFFIX_LENGTH)
            ?.takeIf { it.length == DFU_NAME_SUFFIX_LENGTH && it.all(Char::isDigit) }
            ?: return null
        return "$DFU_NAME_PREFIX$suffix"
    }

    private fun String.shiftLastMacByteBy(delta: Int): String? {
        val tokens = split(MAC_SEPARATOR)
        if (tokens.size != MAC_TOKENS_COUNT) return null
        val currentToken = tokens.last()
        val current = runCatching { BigInteger(currentToken, HEX_RADIX) }.getOrNull() ?: return null
        val shifted = current + BigInteger.valueOf(delta.toLong())
        if (shifted < BigInteger.ZERO || shifted > MAX_MAC_TOKEN) return null
        val shiftedToken = shifted.toString(HEX_RADIX).padStart(MAC_TOKEN_LENGTH, '0').takeLast(MAC_TOKEN_LENGTH)
        return tokens.dropLast(1).plus(shiftedToken).joinToString(MAC_SEPARATOR).uppercase()
    }

    private fun String.isUnknownCommand(): Boolean =
        contains(UNKNOWN_COMMAND_RESPONSE, ignoreCase = true)

    private fun matchesLastSyncedMeasurement(
        currentMeasurement: String,
        lastSyncEvent: String?
    ): Boolean {
        if (lastSyncEvent.isNullOrBlank()) return false
        if (currentMeasurement == lastSyncEvent) return true

        val currentIdentity = currentMeasurement.toMeasurementIdentity() ?: return false
        val lastIdentity = lastSyncEvent.toMeasurementIdentity() ?: return false
        return currentIdentity.glucoseX10 == lastIdentity.glucoseX10 &&
                abs(currentIdentity.epochSeconds - lastIdentity.epochSeconds) <= LAST_EVENT_TIME_TOLERANCE_SECONDS
    }

    private fun String.toMeasurementIdentity(): MeasurementIdentity? {
        RD_IDENTITY_REGEX.matchEntire(this)?.let { match ->
            val dateToken = match.groupValues[1]
            val valueToken = match.groupValues[3]
            val epochSeconds = runCatching {
                dateToken.fromGlucometerDateTime().toEpochSecond()
            }.getOrNull() ?: return null
            return MeasurementIdentity(
                epochSeconds = epochSeconds,
                glucoseX10 = valueToken.toInt()
            )
        }

        MEM_IDENTITY_REGEX.matchEntire(this)?.let { match ->
            val unixHex = match.groupValues[1]
            val valueHex = match.groupValues[3]
            return MeasurementIdentity(
                epochSeconds = unixHex.toLong(HEX_RADIX),
                glucoseX10 = valueHex.toInt(HEX_RADIX)
            )
        }
        return null
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

        val toBootModeResult = glucometerBleManager.toBootMode()
        if (!toBootModeResult.isOk()) {
            crashlyticsReport.writeException(GlucometerToDfuModeError)
            throw GlucometerToDfuModeError
        }
        crashlyticsReport.log("The device has successfully switched to dfu mode")
    }

    override suspend fun sendFirmwareChunk(chuck: FirmwareChunk): String {
        return glucometerBleManager.sendFirmwareChunk(chuck)
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

    @Throws(GlucometerNotConnectedException::class)
    private fun checkIsConnected(address: String) {
        if (!glucometerBleManager.isConnected(address)) {
            val error = GlucometerNotConnectedException(address)
            crashlyticsReport.writeException(error)
            throw error
        }
    }

}

private const val MIN_BATTERY_LEVEL = 1
private const val MAX_GLUCOSE_EVENTS_COUNT = 1000
private const val UNKNOWN_COMMAND_RESPONSE = "unknown command"
private const val DFU_NAME_PREFIX = "Dfu"
private const val DFU_NAME_SUFFIX_LENGTH = 4
private const val MAC_SEPARATOR = ":"
private const val MAC_TOKENS_COUNT = 6
private const val MAC_TOKEN_LENGTH = 2
private const val HEX_RADIX = 16
private const val LAST_EVENT_TIME_TOLERANCE_SECONDS = 1L
private const val MAX_ALLOWED_TIME_DELTA_SECONDS = 60L
private val MAX_MAC_TOKEN = BigInteger("FF", HEX_RADIX)
private val RD_IDENTITY_REGEX = Regex("^rd(\\d{12})(\\d{3})(\\d{3})$", RegexOption.IGNORE_CASE)
private val MEM_IDENTITY_REGEX =
    Regex("^mem\\.([0-9A-F]{8})([0-9A-F]{4})([0-9A-F]{4})$", RegexOption.IGNORE_CASE)

private object UnknownGetMemCommandException : Exception("getmem command is not supported")

private const val HARDWARE_ERROR_MASK = 0x7FL

object GlucometerDebugConfig {
    /**
     * Флаг для симуляции аппаратной ошибки глюкометра (Таблица №3).
     */
    var MOCK_HARDWARE_ERROR: Boolean
        get() = com.elta.android.common.errors.GlucometerTestConfig.MOCK_HARDWARE_ERROR
        set(value) {
            com.elta.android.common.errors.GlucometerTestConfig.MOCK_HARDWARE_ERROR = value
        }
}




private data class MeasurementIdentity(
    val epochSeconds: Long,
    val glucoseX10: Int
)


