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
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.jakewharton.rxrelay2.PublishRelay
import com.nullgr.core.intents.launchForResult
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import me.dmdev.rxpm.PresentationModel

class PermissionsControl2VariantA {

    internal val bluetoothRequestRelay = PublishRelay.create<Unit>()
    internal val bluetoothRequestResultRelay = PublishRelay.create<Boolean>()

    internal val bluetoothPermissionsRequestRelay = PublishRelay.create<Unit>()
    internal val bluetoothPermissionsRequestResultRelay = PublishRelay.create<Boolean>()

    internal val combinedPermissionsRequestRelay = PublishRelay.create<Unit>()
    internal val combinedPermissionsRequestResultRelay = PublishRelay.create<Boolean>()

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            bluetoothPermissionsRequestResultRelay
                .doOnSubscribe { bluetoothPermissionsRequestRelay.accept(Unit) }
                .firstElement()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            combinedPermissionsRequestResultRelay
                .doOnSubscribe { combinedPermissionsRequestRelay.accept(Unit) }
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

fun PresentationModel.bluetoothControl2VariantA(): PermissionsControl2VariantA = PermissionsControl2VariantA()

fun PermissionsControl2VariantA.bindTo(
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
            permissions.request(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        }
        .subscribe(bluetoothPermissionsRequestResultRelay)
        .addTo(compositeUnbind)

    combinedPermissionsRequestRelay
        .observeOn(AndroidSchedulers.mainThread())
        .switchMap {
            permissions.request(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        .subscribe(combinedPermissionsRequestResultRelay)
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
            enableLocation(fragment)
        }
        .addTo(compositeUnbind)
}

fun PermissionsControl2VariantA.resolveResults(requestCode: Int, resultCode: Int) {
    if (requestCode in arrayOf(PermissionsControl2.REQUEST_CODE_ENABLE_LOCATION, PermissionsControl.REQUEST_CODE_ENABLE_LOCATION)) {
        locationRequestResultRelay.accept(resultCode == Activity.RESULT_OK)
    }

    if (requestCode == PermissionsControl2.REQUEST_CODE_ENABLE_BLUETOOTH) {
        bluetoothRequestResultRelay.accept(resultCode == Activity.RESULT_OK)
    }
}

fun enableLocation(fragment: Fragment, onEnabled: (() -> Unit)? = null) {
    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
        .setAlwaysShow(true)

    val client = LocationServices.getSettingsClient(fragment.requireContext())
    val task = client.checkLocationSettings(builder.build())

    task.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            try {
                exception.startResolutionForResult(
                    fragment.requireActivity(),
                    PermissionsControl.REQUEST_CODE_ENABLE_LOCATION
                )
            } catch (sendEx: IntentSender.SendIntentException) {
                // Обработка ошибки при отправке интента
            }
        }
    }
        .addOnSuccessListener {
            onEnabled?.invoke()
        }

}

fun checkPermissionsVariantA(activity: Activity) {
    fun check(permissions: List<String>) {
        if (permissions.isEmpty()) return
        permissions.map {
            ActivityCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }.firstOrNull { it }?.let {
            requestPermissions(
                activity,
                permissions.toTypedArray(),
                1
            )
        }
    }

    val androidVersion = Build.VERSION.SDK_INT

    val permissionsList = mutableListOf<String>()

    if (androidVersion >= Build.VERSION_CODES.TIRAMISU) {
        permissionsList.add(Manifest.permission.POST_NOTIFICATIONS)
    }
    if (androidVersion >= Build.VERSION_CODES.S) {
        permissionsList.add(Manifest.permission.BLUETOOTH_SCAN)
        permissionsList.add(Manifest.permission.BLUETOOTH_CONNECT)
    }
    if (androidVersion < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    check(permissionsList)
}

private fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
    ActivityCompat.requestPermissions(activity, permissions, requestCode)
}
