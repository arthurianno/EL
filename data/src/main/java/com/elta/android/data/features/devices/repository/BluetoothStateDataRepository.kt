package com.elta.android.data.features.devices.repository

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.core.location.LocationManagerCompat
import com.elta.android.common.logger.crashlyrics.CrashlyticsReport
import com.elta.android.domain.features.devices.repository.BluetoothStateRepository
import javax.inject.Inject

class BluetoothStateDataRepository @Inject constructor(
    private val context: Context,
    private val adapter: BluetoothAdapter,
    private val locationManager: LocationManager,
    private val crashlyticsReport: CrashlyticsReport
) : BluetoothStateRepository {

    override fun isBluetoothPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val bluetoothConnectIsGranted = checkPermission(Manifest.permission.BLUETOOTH_CONNECT)
            val bluetoothScanIsGranted = checkPermission(Manifest.permission.BLUETOOTH_SCAN)

            crashlyticsReport.log(
                "Permission scan granted: $bluetoothScanIsGranted; connect granted: $bluetoothConnectIsGranted"
            )

            bluetoothConnectIsGranted && bluetoothScanIsGranted
        } else {
            val accessFineLocationIsGranted = checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            crashlyticsReport.log("Permission fine location granted: $accessFineLocationIsGranted")
            accessFineLocationIsGranted
        }
    }

    override fun isLocationPermissionGranted(): Boolean {
        if (!isLegacyBleLocationRequired) return true

        val granted = checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        crashlyticsReport.log("Permission fine location granted: $granted")
        return granted
    }


    override fun isBluetoothEnabled(): Boolean {
        val bluetoothIsEnable = adapter.isEnabled

        crashlyticsReport.log("Bluetooth is enabled: $bluetoothIsEnable")

        return bluetoothIsEnable
    }

    override fun isLocationEnabled(): Boolean {
        if (!isLegacyBleLocationRequired) return true

        val enabled = LocationManagerCompat.isLocationEnabled(locationManager)
        crashlyticsReport.log("Location is enabled: $enabled")
        return enabled
    }

    private val isLegacyBleLocationRequired: Boolean
        get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.S

    private fun checkPermission(permissionName: String): Boolean =
        context.checkSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED
}
