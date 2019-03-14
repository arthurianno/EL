package com.elta.android.presentation.features.bluetooth.pm

import com.elta.android.presentation.core.pm.BasePm
import com.elta.android.presentation.core.pm.ServiceFacade
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
import timber.log.Timber
import java.io.OutputStreamWriter
import java.util.Scanner
import javax.inject.Inject

class BluetoothPm @Inject constructor(
    services: ServiceFacade
) : BasePm(services) {

    private val scanner = BluetoothLeScannerCompat.getScanner()
    private val settings = ScanSettings.Builder()
        .setLegacy(false)
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setNumOfMatches(1)
        .setReportDelay(1000)
        .setUseHardwareBatchingIfSupported(true)
        .build()
    private val filters = listOf<ScanFilter>(ScanFilter.Builder().setDeviceName("SatelliteOnline").build())
    private val callback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            Timber.tag("SCAN").d("onScanFailed $errorCode")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            Timber.tag("SCAN").d("onScanResult $callbackType, $result")
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            Timber.tag("SCAN").d("onBatchScanResults")
            results.forEach {
                Timber.tag("SCAN").d("result: ${it.device.name}, ${it.device.address}")
            }
            if (results.isNotEmpty()) {
                val device = results[0].device
                val bounded = device.createBond()
                if(bounded) {
                    val socket = device.createInsecureRfcommSocketToServiceRecord(device.uuids[0].uuid)
                    val scanner = Scanner(socket.inputStream)
                    val writer = OutputStreamWriter(socket.outputStream)
                    socket.
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        scanner.startScan(filters, settings, callback)
    }

    override fun onDestroy() {
        super.onDestroy()
        scanner.stopScan(callback)
    }
}