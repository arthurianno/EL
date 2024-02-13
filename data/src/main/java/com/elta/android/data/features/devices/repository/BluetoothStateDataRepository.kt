package com.elta.android.data.features.devices.repository

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.domain.features.devices.repository.BluetoothStateRepository
import javax.inject.Inject

class BluetoothStateDataRepository @Inject constructor(
    private val context: Context,
    private val adapter: BluetoothAdapter,
    private val locationManager: LocationManager,
    private val crashlyticsReport: CrashlyticsReport
) : BluetoothStateRepository {

    private fun checkLocationPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
        val accessFineLocationIsGranted =
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        crashlyticsReport.log(
            "Permission fine location granted: $accessFineLocationIsGranted"
        )

        return accessFineLocationIsGranted
    }

    private fun checkBluetoothPermissions(): Boolean {
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
            true
        }
    }

    override fun isPermissionGranted(): Boolean {
        return checkLocationPermissions() && checkBluetoothPermissions()
    }

    override fun isBluetoothEnabled(): Boolean {
        val bluetoothIsEnable = adapter.isEnabled

        crashlyticsReport.log("Bluetooth is enabled: $bluetoothIsEnable")

        return bluetoothIsEnable
    }

    override fun isLocationEnabledPre34Api(): Boolean {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) return true
        val locationIsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        crashlyticsReport.log("Location is enabled: $locationIsEnabled")

        return locationIsEnabled
    }
}

private const val TAG = "BluetoothState"
