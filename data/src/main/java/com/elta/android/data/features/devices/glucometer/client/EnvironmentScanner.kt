package com.elta.android.data.features.devices.glucometer.client

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.elta.android.common.errors.BluetoothScannerNotAvailable
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnvironmentScanner @Inject constructor(
    private val adapter: BluetoothAdapter,
    private val context: Context,
    private val crashlyticsReport: CrashlyticsReport
) {
    private var callback: ScanCallback? = null

    @SuppressLint("MissingPermission")
    fun startScan(
        filters: List<ScanFilter>,
        settings: ScanSettings,
        resultCallback: (List<ScanResult>) -> Unit
    ) {
        stopScan()

        callback = object : ScanCallback() {

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.let {
                    crashlyticsReport.log("onScanNotFilteredResult: ${result.device.address}")
                    if (result.isFiltered(filters)) {
                        crashlyticsReport.log("onScanFilteredResult: ${result.device.address}")
                        resultCallback(listOf(result))
                    }
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                crashlyticsReport.log(
                    "onScanNotFilteredResult: ${
                        results.map {
                            it.device.address + ", "
                        }
                    }"
                )
                val list = results.filter {
                    it.isFiltered(filters)
                }
                if (list.isNotEmpty()) {
                    crashlyticsReport.log(
                        "onScanFilteredResult: ${
                            list.map {
                                it.device.address + ", "
                            }
                        }"
                    )
                    resultCallback(list)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                val error = ScanError(errorCode)
                crashlyticsReport.writeException(error)
                throw error
            }
        }
        val scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            crashlyticsReport.log("bluetooth scanner not available during scan start")
            throw BluetoothScannerNotAvailable
        }

        crashlyticsReport.log("start scanning")
        adapter.bluetoothLeScanner.startScan(emptyList(), settings, callback)
    }

    fun stopScan() {
        crashlyticsReport.log("stop scanning start")
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        }

        crashlyticsReport.log("scanner permission state: $permission")

        if (permission) {
            callback?.let {
                adapter.bluetoothLeScanner?.stopScan(it)
                crashlyticsReport.log("stop scanning success")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun ScanResult.isFiltered(filters: List<ScanFilter>): Boolean {
        val deviceName = device.name ?: scanRecord?.deviceName
        filters.forEach { filter ->
            val nameToFilter = filter.deviceName
            return nameToFilter != null && deviceName != null && deviceName.contains(nameToFilter)
        }
        return false
    }

    data class ScanError(val code: Int) : RuntimeException()

}

private const val TAG = "ScannerService"

private const val SCAN_TIMEOUT_SECOND = 60L
