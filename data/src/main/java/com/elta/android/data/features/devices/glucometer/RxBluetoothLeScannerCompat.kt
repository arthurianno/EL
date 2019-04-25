package com.elta.android.data.features.devices.glucometer

import io.reactivex.Observable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import java.util.concurrent.TimeUnit

private const val SCAN_TIMEOUT = 60L // seconds

fun BluetoothLeScannerCompat.startScan(
    filters: List<ScanFilter> = emptyList(),
    settings: ScanSettings
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

    // pass empty list to organize own filter
    startScan(emptyList(), settings, callback)

    emitter.setDisposable(Disposables.fromAction {
        stopScan(callback)
    })
}
    .filter { it.isFiltered(filters) }
    .scan(mutableSetOf<ScanResult>()) { results, result ->
        results.add(result)
        results.distinctBy(ScanResult::getDevice).toMutableSet()
    }
    .map(MutableSet<ScanResult>::toList)
    .distinctUntilChanged { r1, r2 -> !r1.isResultChanged(r2) }
    .skip(1)
    .timeout(SCAN_TIMEOUT, TimeUnit.SECONDS, Schedulers.computation())

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

fun ScanResult.isFiltered(filters: List<ScanFilter>): Boolean {
    val deviceName = device.name ?: scanRecord?.deviceName
    filters.forEach { filter ->
        val nameToFilter = filter.deviceName
        return nameToFilter != null && deviceName != null && deviceName.contains(nameToFilter)
    }
    return false
}

data class ScanError(val code: Int) : RuntimeException()