@file:Suppress("UseDataClass")

package com.elta.android.presentation.features.sync.control

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentSender
import android.support.v4.app.Fragment
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

fun BluetoothControl2.bindTo(compositeUnbind: CompositeDisposable, permissions: RxPermissions, fragment: Fragment) {
    bluetoothRequestRelay
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                .launchForResult(checkNotNull(fragment.activity), BluetoothControl2.REQUEST_CODE_ENABLE_BLUETOOTH)
        }
        .addTo(compositeUnbind)
    locationPermissionsRequestRelay
        .observeOn(AndroidSchedulers.mainThread())
        .switchMap {
            permissions.request(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        .subscribe(locationPermissionsRequestResultRelay)
        .addTo(compositeUnbind)

    locationRequestRelay
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
    val result = SettingsClient(checkNotNull(fragment.context))
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
                            checkNotNull(fragment.activity),
                            BluetoothControl2.REQUEST_CODE_ENABLE_LOCATION
                        )
                    } catch (e1: IntentSender.SendIntentException) {
                        Timber.e(e1)
                    }
            }
        }
    }
}