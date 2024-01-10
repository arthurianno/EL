package com.elta.android.data.features.devices.glucometer

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
import io.reactivex.Observable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

private const val SCAN_TIMEOUT_SECOND = 60L

fun BluetoothLeScanner.startScan(
    filters: List<ScanFilter> = emptyList(),
    settings: ScanSettings,
    context: Context
): Observable<List<ScanResult>> = Observable.create<ScanResult> { emitter ->
    val callback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (!emitter.isDisposed) {
                emitter.onNext(result)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            if (!emitter.isDisposed) {
                results.forEach { result ->
                    emitter.onNext(result)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            if (!emitter.isDisposed) {
                emitter.onError(ScanError(errorCode))
            }
        }
    }

    val permission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.BLUETOOTH
    ) == PackageManager.PERMISSION_GRANTED
    if (permission) {
        // pass empty list to organize own filter //TODO: Проверить зачем так сделано
        startScan(emptyList(), settings, callback)
    }

    emitter.setDisposable(
        Disposables.fromAction {
            stopScan(callback)
        }
    )
}
    .filter { it.isFiltered(filters) } //TODO: Проверить зачем так сделано
    .scan(mutableSetOf<ScanResult>()) { results, result ->
        results.add(result)
        results.distinctBy(ScanResult::getDevice).toMutableSet()
    }
    .map(MutableSet<ScanResult>::toList)
    .doOnNext { Timber.i("<<<<<<<ScanResults>>>>>>  ScanResults: $it") }
    .distinctUntilChanged { r1, r2 -> !r1.isResultChanged(r2) }
    .skip(1)
    .timeout(SCAN_TIMEOUT_SECOND, TimeUnit.SECONDS, Schedulers.computation())

@Suppress("ReturnCount")
fun List<ScanResult>.isResultChanged(other: List<ScanResult>): Boolean {
    if (size != other.size) {
        return true
    }

    forEachIndexed { index, item ->
        return item.device != other[index].device
    }

    return false
}

@SuppressLint("MissingPermission")
fun ScanResult.isFiltered(filters: List<ScanFilter>): Boolean {
    val deviceName = device.name ?: scanRecord?.deviceName
    filters.forEach { filter ->
        val nameToFilter = filter.deviceName
        return nameToFilter != null && deviceName != null && deviceName.contains(nameToFilter)
    }
    return false
}

data class ScanError(val code: Int) : RuntimeException()
