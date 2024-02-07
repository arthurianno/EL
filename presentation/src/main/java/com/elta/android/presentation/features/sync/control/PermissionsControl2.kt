@file:Suppress("UseDataClass")

package com.elta.android.presentation.features.sync.control

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import com.jakewharton.rxrelay2.PublishRelay
import com.nullgr.core.intents.launchForResult
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import me.dmdev.rxpm.PresentationModel
import timber.log.Timber

class PermissionsControl2 {

    internal val bluetoothRequestRelay = PublishRelay.create<Unit>()
    internal val bluetoothRequestResultRelay = PublishRelay.create<Boolean>()

    internal val bluetoothPermissionsRequestRelay = PublishRelay.create<Unit>()
    internal val bluetoothPermissionsRequestResultRelay = PublishRelay.create<Boolean>()

    internal val locationRequestRelay = PublishRelay.create<Unit>()
    internal val locationRequestResultRelay = PublishRelay.create<Boolean>()

    internal val locationPermissionsRequestRelay = PublishRelay.create<Unit>()
    internal val locationPermissionsRequestResultRelay = PublishRelay.create<Boolean>()

    internal lateinit var bluetoothDisposable: Disposable
    internal lateinit var locationDisposable: Disposable

    fun requestEnableBluetooth(): Maybe<Boolean> =
        bluetoothRequestResultRelay
            .doOnSubscribe { bluetoothRequestRelay.accept(Unit) }
            .firstElement()

    fun requestEnableLocation(): Maybe<Boolean> =
        locationRequestResultRelay
            .doOnSubscribe { locationRequestRelay.accept(Unit) }
            .firstElement()

    fun requestLocationPermissions(): Maybe<Boolean> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothPermissionsRequestResultRelay
                .doOnSubscribe { bluetoothPermissionsRequestRelay.accept(Unit) }
                .firstElement()
        } else {
            locationPermissionsRequestResultRelay
                .doOnSubscribe { locationPermissionsRequestRelay.accept(Unit) }
                .firstElement()
        }

    companion object {
        const val REQUEST_CODE_ENABLE_LOCATION = 147
        const val REQUEST_CODE_ENABLE_BLUETOOTH = 148
    }
}

fun PresentationModel.bluetoothControl2(): PermissionsControl2 = PermissionsControl2()

fun PermissionsControl2.bindTo(
    compositeUnbind: CompositeDisposable,
    permissions: RxPermissions,
    fragment: Fragment
) {
    bluetoothDisposable = bluetoothRequestRelay
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                .launchForResult(
                    fragment.requireActivity(),
                    PermissionsControl2.REQUEST_CODE_ENABLE_BLUETOOTH
                )
        }
        .addTo(compositeUnbind)

    bluetoothPermissionsRequestRelay
        .observeOn(AndroidSchedulers.mainThread())
        .switchMap {
            permissions.request(Manifest.permission.BLUETOOTH_SCAN)
        }
        .subscribe(bluetoothPermissionsRequestResultRelay)
        .addTo(compositeUnbind)

    locationPermissionsRequestRelay
        .observeOn(AndroidSchedulers.mainThread())
        .switchMap {
            permissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        .subscribe(locationPermissionsRequestResultRelay)
        .addTo(compositeUnbind)

    locationDisposable = locationRequestRelay
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            enableLocation2(fragment)
        }
        .addTo(compositeUnbind)
}

fun PermissionsControl2.resolveResults(requestCode: Int, resultCode: Int) {
    if (requestCode == PermissionsControl2.REQUEST_CODE_ENABLE_LOCATION) {
        locationRequestResultRelay.accept(resultCode == Activity.RESULT_OK)
    }

    if (requestCode == PermissionsControl2.REQUEST_CODE_ENABLE_BLUETOOTH) {
        bluetoothRequestResultRelay.accept(resultCode == Activity.RESULT_OK)
    }
}

fun enableLocation2(fragment: Fragment) {
    val result = SettingsClient(fragment.requireContext())
        .checkLocationSettings(
            LocationSettingsRequest.Builder()
                .addLocationRequest(LocationRequest.create())
                .setNeedBle(true)
                .build()
        )
    result.addOnCompleteListener { task ->
        try {
            task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            when (e.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                    try {
                        (e as? ResolvableApiException)?.startResolutionForResult(
                            fragment.requireActivity(),
                            PermissionsControl2.REQUEST_CODE_ENABLE_LOCATION
                        )
                    } catch (e1: IntentSender.SendIntentException) {
                        Timber.e(e1)
                    }
            }
        }
    }
}

fun checkPermissions(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        checkNotificationAndBluetooth(activity)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        checkBluetooth(activity)
    }
}

fun checkBluetooth(activity: Activity) {
    val bluetoothScanName = Manifest.permission.BLUETOOTH_SCAN
    val bluetoothConnectName = Manifest.permission.BLUETOOTH_CONNECT

    if (ActivityCompat.checkSelfPermission(activity, bluetoothScanName) !=
        PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(activity, bluetoothConnectName) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        requestPermissions(
            activity,
            arrayOf(bluetoothScanName, bluetoothConnectName),
            1
        )
    }
}

private fun checkNotificationAndBluetooth(activity: Activity) {
    val bluetoothScanName = Manifest.permission.BLUETOOTH_SCAN
    val bluetoothConnectName = Manifest.permission.BLUETOOTH_CONNECT
    val notificationName = Manifest.permission.POST_NOTIFICATIONS

    if (ActivityCompat.checkSelfPermission(activity, notificationName) !=
        PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(activity, bluetoothScanName) !=
        PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(activity, bluetoothConnectName) !=
        PackageManager.PERMISSION_GRANTED
    ) {
        requestPermissions(
            activity,
            arrayOf(notificationName, bluetoothScanName, bluetoothConnectName),
            100
        )
    }
}

private fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
    ActivityCompat.requestPermissions(activity, permissions, requestCode)
}
