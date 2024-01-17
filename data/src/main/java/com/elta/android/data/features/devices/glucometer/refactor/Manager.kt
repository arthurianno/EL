package com.elta.android.data.features.devices.glucometer.refactor

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class Manager @Inject constructor(
    private val scannerService: ScannerService,
    private val glucometerBleManager: GlucometerBleManager,
) {

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

    fun scan(): Flow<List<ScanResult>> {
        return callbackFlow {
            scannerService.startScan(filters, settings) {
                trySend(it)
            }
            awaitClose {
                scannerService.stopScan()
            }
        }
    }

    suspend fun scan(address: String): ScanResult {
        return suspendCoroutine { continuation ->
            scannerService.startScan(filters = filters, settings = settings) { scanResults ->
                scanResults.firstOrNull { it.device.address == address }?.let { result ->
                    try { //TODO: вызывается второй раз с исключением
                        continuation.resume(result)
                    } catch (ex: IllegalStateException) {
                        scannerService.stopScan()
                    } finally {
                        scannerService.stopScan()
                    }
                }
            }
        }
    }

    suspend fun connectDevice(address: String, pin: String) {
        val scanResult = scan(address)
        Timber.tag(TAG).d("ScanResult: $scanResult")
        //TODO: возможно стоит проверять на connect перед операцией и только тогда его производить
        glucometerBleManager.connectToGlucometer(scanResult.device)
        glucometerBleManager.checkPin(pin)
        glucometerBleManager.getDate()
        glucometerBleManager.getVersion()
        glucometerBleManager.getBatteryAndTemperature()
        glucometerBleManager.turnOnFindMode()
        glucometerBleManager.updateTime(ZonedDateTime.now())
        glucometerBleManager.getSerialNumber()
        glucometerBleManager.readEvent(0)
        glucometerBleManager.toDfuMode()
        glucometerBleManager.disconnectGlucometer()
    }

}

private const val TAG = "BLE_MANAGER"