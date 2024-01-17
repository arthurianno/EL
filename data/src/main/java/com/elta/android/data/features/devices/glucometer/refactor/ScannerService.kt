package com.elta.android.data.features.devices.glucometer.refactor

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.suspendCoroutine

@Singleton
class ScannerService @Inject constructor(
    private val scanner: BluetoothLeScanner,
    private val context: Context,
) {
    private var callback: ScanCallback? = null

    fun startScan(
        filters: List<ScanFilter>,
        settings: ScanSettings,
        resultCallback: (List<ScanResult>) -> Unit
    ) {
        stopScan()

        callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                resultCallback(listOf(result))
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                val result = results.firstOrNull { it.isFiltered(filters) }
                if (result != null) {
                    val permission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.BLUETOOTH
                    ) == PackageManager.PERMISSION_GRANTED
                    if (permission) {
                        scanner.stopScan(callback)
                    }
                } else {
                    //TIMER to STOP!
                    //        .timeout(SCAN_TIMEOUT_SECOND, TimeUnit.SECONDS, Schedulers.computation())
                }

                resultCallback(results)
            }

            override fun onScanFailed(errorCode: Int) {
                throw ScanError(errorCode)
            }
        }

        val permission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED
        if (permission) {
            // pass empty list to organize own filter
            scanner.startScan(emptyList(), settings, callback)
        }
    }

    fun stopScan() {
        val permission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.BLUETOOTH
        ) == PackageManager.PERMISSION_GRANTED
        if (permission) {
            callback?.let { scanner.stopScan(it) }
        }
    }


    @Suppress("ReturnCount")
    private fun List<ScanResult>.isResultChanged(other: List<ScanResult>): Boolean {
        if (size != other.size) {
            return true
        }

        forEachIndexed { index, item ->
            return item.device != other[index].device
        }

        return false
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

private const val SCAN_TIMEOUT_SECOND = 60L
