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
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnvironmentScanner @Inject constructor(
    private val adapter: BluetoothAdapter,
    private val context: Context
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
        val scanner = adapter.bluetoothLeScanner
        if (scanner == null) {
            //TODO: в логи, проверить состояние блютуз и разрешения
            throw BluetoothScannerNotAvailable
        }

        adapter.bluetoothLeScanner.startScan(emptyList(), settings, callback)
    }

    fun stopScan() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        }

        Timber.tag(TAG).d("scanner permission state: $permission")

        if (permission) {
            callback?.let { adapter.bluetoothLeScanner?.stopScan(it) }
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
