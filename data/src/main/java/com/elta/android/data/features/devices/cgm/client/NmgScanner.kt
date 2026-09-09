package com.elta.android.data.features.devices.cgm.client

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import com.elta.android.data.features.devices.cgm.protocol.NmgAdvertisementParser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NmgScanner @Inject constructor(
    private val bluetoothAdapter: BluetoothAdapter
) {
    @SuppressLint("MissingPermission")
    fun findSensors(): Flow<List<String>> = callbackFlow {
        val scanner = bluetoothAdapter.bluetoothLeScanner
            ?: run {
                close(IllegalStateException("Bluetooth LE scanner is unavailable"))
                return@callbackFlow
            }
        val sensors = linkedSetOf<String>()
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val record = result.scanRecord ?: return
                if (!record.deviceName.equals(NMG_DEVICE_NAME, ignoreCase = true)) return
                val sensor = NmgAdvertisementParser.parse(record) ?: return
                if (sensors.add(sensor.sensorId)) trySend(sensors.toList())
            }

            override fun onScanFailed(errorCode: Int) {
                close(IllegalStateException("NMG scan failed: $errorCode"))
            }
        }
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()
        try {
            scanner.startScan(emptyList(), settings, callback)
        } catch (error: SecurityException) {
            close(error)
        }
        awaitClose { runCatching { scanner.stopScan(callback) } }
    }

    private companion object {
        const val NMG_DEVICE_NAME = "ELTA"
    }
}
