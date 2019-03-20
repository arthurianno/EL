package com.elta.android.presentation.features.bluetooth

import com.elta.android.common.utils.log
import io.reactivex.Observable
import io.reactivex.disposables.Disposables
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings

fun BluetoothLeScannerCompat.startScan(
    filters: List<ScanFilter>? = emptyList(),
    settings: ScanSettings
): Observable<List<ScanResult>> = Observable.create<List<ScanResult>> { emitter ->
    val callback = object : ScanCallback() {

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            if (!emitter.isDisposed) {
                emitter.onNext(results)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            if (!emitter.isDisposed) {
                emitter.onError(ScanError(errorCode))
            }
        }
    }

    startScan(filters, settings, callback)

    emitter.setDisposable(Disposables.fromAction {
        stopScan(callback)
    })
}
    .scan(mutableSetOf<ScanResult>()) { results, newResults ->
        results.addAll(newResults)
        results.distinctBy(ScanResult::getDevice).toMutableSet()
    }
    .map(MutableSet<ScanResult>::toList)
    .distinctUntilChanged { r1, r2 -> !r1.isChanged(r2) }
    .log("Scan", "size") { it.size.toString() }

@Suppress("ReturnCount")
fun List<ScanResult>.isChanged(other: List<ScanResult>): Boolean {
    if (size != other.size) {
        return true
    }

    forEachIndexed { index, item ->
        return item.device != other[index].device
    }

    return false
}

data class ScanError(val code: Int) : RuntimeException()