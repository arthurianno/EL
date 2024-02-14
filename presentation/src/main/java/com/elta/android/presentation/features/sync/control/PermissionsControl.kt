@file:Suppress("UseDataClass")

package com.elta.android.presentation.features.sync.control

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.fragment.app.Fragment
import com.nullgr.core.intents.launchForResult
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import me.dmdev.rxpm.PresentationModel
import me.dmdev.rxpm.action
import me.dmdev.rxpm.command

class PermissionsControl(pm: PresentationModel) {

    val requestEnableBluetoothCommand = pm.command<Unit>(bufferSize = 1)
    val requestBluetoothPermissionCommand = pm.command<Unit>(bufferSize = 1)
    val requestLocationPermissionsCommand = pm.command<Unit>(bufferSize = 1)
    val requestCombinedPermissionsCommand = pm.command<Unit>(bufferSize = 1)
    val requestEnableLocationCommand = pm.command<Unit>(bufferSize = 1)

    val bluetoothEnabledAction = pm.action<Unit>()
    val bluetoothDeniedAction = pm.action<Unit>()
    val locationPermissionsGrantedAction = pm.action<Permission>()
    val bluetoothPermissionsGrantedAction = pm.action<Permission>()
    val locationEnabledAction = pm.action<Unit>()
    val locationDeniedAction = pm.action<Unit>()

    companion object {
        const val REQUEST_CODE_ENABLE_LOCATION = 145
        const val REQUEST_CODE_ENABLE_BLUETOOTH = 146
    }
}

fun PresentationModel.bluetoothControl(): PermissionsControl = PermissionsControl(this)

fun PermissionsControl.bindTo(
    compositeUnbind: CompositeDisposable,
    permissions: RxPermissions,
    fragment: Fragment
) {
    requestEnableBluetoothCommand.observable
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                .launchForResult(
                    fragment.requireActivity(),
                    PermissionsControl.REQUEST_CODE_ENABLE_BLUETOOTH
                )
        }
        .addTo(compositeUnbind)

    requestBluetoothPermissionCommand.observable
        .observeOn(AndroidSchedulers.mainThread())
        .switchMap {
            permissions.requestEach(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT
            )
        }
        .subscribe(bluetoothPermissionsGrantedAction.consumer)
        .addTo(compositeUnbind)

    requestLocationPermissionsCommand.observable
        .observeOn(AndroidSchedulers.mainThread())
        .switchMap {
            permissions.requestEach(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        .subscribe(locationPermissionsGrantedAction.consumer)
        .addTo(compositeUnbind)

    requestCombinedPermissionsCommand.observable
        .observeOn(AndroidSchedulers.mainThread())
        .switchMap {
            permissions.requestEach(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        .filter { !it.granted }
        .subscribe {
            if (it.name == android.Manifest.permission.ACCESS_FINE_LOCATION) {
                locationPermissionsGrantedAction.consumer.accept(it)
            } else {
                bluetoothPermissionsGrantedAction.consumer.accept(it)
            }
        }
        .addTo(compositeUnbind)

    requestEnableLocationCommand.observable
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            enableLocation(fragment)
        }
        .addTo(compositeUnbind)
}

fun PermissionsControl.resolveResults(requestCode: Int, resultCode: Int) {
    if (requestCode == PermissionsControl.REQUEST_CODE_ENABLE_LOCATION) {
        if (resultCode == Activity.RESULT_OK) {
            locationEnabledAction.consumer.accept(Unit)
        } else {
            locationDeniedAction.consumer.accept(Unit)
        }

    }

    if (requestCode == PermissionsControl.REQUEST_CODE_ENABLE_BLUETOOTH) {
        if (resultCode == Activity.RESULT_OK) {
            bluetoothEnabledAction.consumer.accept(Unit)
        } else {
            bluetoothDeniedAction.consumer.accept(Unit)
        }
    }
}
