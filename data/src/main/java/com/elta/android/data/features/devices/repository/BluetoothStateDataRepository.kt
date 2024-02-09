package com.elta.android.data.features.devices.repository

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.domain.features.devices.repository.BluetoothStateRepository
import timber.log.Timber
import javax.inject.Inject

class BluetoothStateDataRepository @Inject constructor(
    private val context: Context,
    private val adapter: BluetoothAdapter,
    private val crashlyticsReport: CrashlyticsReport
) : BluetoothStateRepository {

    override fun isPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            val bluetoothConnectIsGranted =
                context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            val bluetoothScanIsGranted =
                context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED

            crashlyticsReport.log(
                "Permission scan granted: $bluetoothScanIsGranted; " +
                        "connect granted: $bluetoothConnectIsGranted"
            )

            bluetoothConnectIsGranted && bluetoothScanIsGranted
        } else {
            val accessFineLocationIsGranted =
                context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

            crashlyticsReport.log(
                "Permission fine location granted: $accessFineLocationIsGranted"
            )

            accessFineLocationIsGranted
        }
    }

    override fun isBluetoothEnable(): Boolean {
        val bluetoothIsEnable = adapter.isEnabled

        crashlyticsReport.log("Bluetooth is enabled: $bluetoothIsEnable")

        return bluetoothIsEnable
    }
}

private const val TAG = "BluetoothState"
