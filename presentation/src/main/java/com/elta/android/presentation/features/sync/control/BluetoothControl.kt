@file:Suppress("UseDataClass")

package com.elta.android.presentation.features.sync.control

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentSender
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import com.nullgr.core.intents.launchForResult
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command
import timber.log.Timber

class BluetoothControl(pm: PresentationModel) {

    val requestEnableBluetoothCommand = pm.command<Unit>(bufferSize = 1)
    val requestLocationPermissionsCommand = pm.command<Unit>(bufferSize = 1)
    val requestEnableLocationCommand = pm.command<Unit>(bufferSize = 1)

    val bluetoothEnabledAction = pm.action<Unit>()
    val locationPermissionsGrantedAction = pm.action<Unit>()
    val locationEnabledAction = pm.action<Unit>()

    companion object {
        const val REQUEST_CODE_ENABLE_LOCATION = 145
        const val REQUEST_CODE_ENABLE_BLUETOOTH = 146
    }
}

fun PresentationModel.bluetoothControl(): BluetoothControl = BluetoothControl(this)

fun BluetoothControl.bindTo(
    compositeUnbind: CompositeDisposable,
    permissions: RxPermissions,
    fragment: Fragment
) {
    requestEnableBluetoothCommand.observable
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                .launchForResult(
                    checkNotNull(fragment.activity),
                    BluetoothControl.REQUEST_CODE_ENABLE_BLUETOOTH
                )
        }
        .addTo(compositeUnbind)
    requestLocationPermissionsCommand.observable
        .observeOn(AndroidSchedulers.mainThread())
        .switchMap {
            permissions.request(android.Manifest.permission.ACCESS_FINE_LOCATION)
                .filter { it }
                .map { Unit }
        }
        .subscribe(locationPermissionsGrantedAction.consumer)
        .addTo(compositeUnbind)

    requestEnableLocationCommand.observable
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            enableLocation(fragment)
        }
        .addTo(compositeUnbind)
}

fun BluetoothControl.resolveResults(requestCode: Int, resultCode: Int) {
    if (requestCode == BluetoothControl.REQUEST_CODE_ENABLE_LOCATION && resultCode == Activity.RESULT_OK) {
        locationEnabledAction.consumer.accept(Unit)
    }

    if (requestCode == BluetoothControl.REQUEST_CODE_ENABLE_BLUETOOTH && resultCode == Activity.RESULT_OK) {
        bluetoothEnabledAction.consumer.accept(Unit)
    }
}

fun enableLocation(fragment: Fragment) {
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
                            BluetoothControl.REQUEST_CODE_ENABLE_LOCATION
                        )
                    } catch (e1: IntentSender.SendIntentException) {
                        Timber.e(e1)
                    }
            }
        }
    }
}
