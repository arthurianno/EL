@file:Suppress("UseDataClass")

package com.elta.android.presentation.features.sync.control

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
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

class BluetoothControl2 {

    internal val bluetoothRequestRelay = PublishRelay.create<Unit>()
    internal val bluetoothRequestResultRelay = PublishRelay.create<Boolean>()

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
        locationPermissionsRequestResultRelay
            .doOnSubscribe { locationPermissionsRequestRelay.accept(Unit) }
            .firstElement()

    companion object {
        const val REQUEST_CODE_ENABLE_LOCATION = 147
        const val REQUEST_CODE_ENABLE_BLUETOOTH = 148
    }
}

fun PresentationModel.bluetoothControl2(): BluetoothControl2 = BluetoothControl2()

fun BluetoothControl2.bindTo(
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
                    BluetoothControl2.REQUEST_CODE_ENABLE_BLUETOOTH
                )
        }
        .addTo(compositeUnbind)
    locationPermissionsRequestRelay
        .observeOn(AndroidSchedulers.mainThread())
        .switchMap {
            permissions.request(android.Manifest.permission.ACCESS_FINE_LOCATION)
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

fun BluetoothControl2.resolveResults(requestCode: Int, resultCode: Int) {
    if (requestCode == BluetoothControl2.REQUEST_CODE_ENABLE_LOCATION) {
        locationRequestResultRelay.accept(resultCode == Activity.RESULT_OK)
    }

    if (requestCode == BluetoothControl2.REQUEST_CODE_ENABLE_BLUETOOTH) {
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
                            BluetoothControl2.REQUEST_CODE_ENABLE_LOCATION
                        )
                    } catch (e1: IntentSender.SendIntentException) {
                        Timber.e(e1)
                    }
            }
        }
    }
}

fun checkBluetoothPermissions(activity: Activity) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        if (ActivityCompat
            .checkSelfPermission(
                    activity,
                    android.Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat
                .checkSelfPermission(
                        activity,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    android.Manifest.permission.BLUETOOTH_SCAN,
                    android.Manifest.permission.BLUETOOTH_CONNECT
                ),
                1
            )
        }
    }
}
