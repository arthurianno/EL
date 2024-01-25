package com.elta.android.data.features.devices.glucometer.refactor

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScannerService @Inject constructor(
    private val scanner: BluetoothLeScanner,
    private val context: Context,
) {
    private var callback: ScanCallback? = null

    @SuppressLint("MissingPermission")
    fun startScan(
        filters: List<ScanFilter>,
        settings: ScanSettings,
        resultCallback: (List<ScanResult>) -> Unit
    ) {
        //FIXME!! Это костыль Макса, т.к каждая комманда сейчас запускается со старта сканирования и подключения
        //В дальнейшем сканирование и подключение должно быть единожды за сессию синхронизации
        stopScan()

        callback = object : ScanCallback() {

            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.let {
                    if (result.isFiltered(filters)) {
                        Timber.tag(TAG).d("onScanResult :: $result")
                        resultCallback(listOf(result))
                    }
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                val list = results.filter { it.isFiltered(filters) }
                if (list.isNotEmpty()) {
                    resultCallback(list)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                throw ScanError(errorCode)
            }
        }

        // todo разобраться с пермишенами
//        val permission = ContextCompat.checkSelfPermission(
//            context, Manifest.permission.BLUETOOTH_SCAN
//        ) == PackageManager.PERMISSION_GRANTED
//        if (permission) {
//            // pass empty list to organize own filter
//            scanner.startScan(emptyList(), settings, callback)
//        }
            scanner.startScan(emptyList(), settings, callback)

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

private const val TAG = "ScannerService"

private const val SCAN_TIMEOUT_SECOND = 60L
