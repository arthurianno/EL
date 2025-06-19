@file:Suppress("UseDataClass")

package com.elta.android.presentation.features.sync.control

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
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

    @Deprecated("Метод некорретно обрабатывает результаты разрешения Устройств поблизости из-за логики библиотеки. Используейте нативные контракты")
    fun requestBluetoothPermission(): Maybe<Boolean> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothPermissionsRequestResultRelay
                .doOnSubscribe { bluetoothPermissionsRequestRelay.accept(Unit) }
                .firstElement()
        } else {
            locationPermissionsRequestResultRelay
                .doOnSubscribe { locationPermissionsRequestRelay.accept(Unit) }
                .firstElement()
        }

    fun requestLocationPermission(): Maybe<Boolean> =
        locationPermissionsRequestResultRelay
                .doOnSubscribe { locationPermissionsRequestRelay.accept(Unit) }
                .firstElement()

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

fun PermissionsControl2.resolveResults(requestCode: Int, resultCode: Int) {
    if (requestCode in arrayOf(PermissionsControl2.REQUEST_CODE_ENABLE_LOCATION, PermissionsControl.REQUEST_CODE_ENABLE_LOCATION)) {
        locationRequestResultRelay.accept(resultCode == Activity.RESULT_OK)
    }

    if (requestCode == PermissionsControl2.REQUEST_CODE_ENABLE_BLUETOOTH) {
        bluetoothRequestResultRelay.accept(resultCode == Activity.RESULT_OK)
    }
}

fun enableLocation(fragment: Fragment) {
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
}

fun ActivityResultLauncher<IntentSenderRequest>.requestEnableLocation(
    context: Context,
    locationAlreadyEnable: (() -> Unit)? = null
) {
    val locationRequest = LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
    val client = LocationServices.getSettingsClient(context)
    val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

    task.addOnFailureListener { exception ->
        if (exception is ResolvableApiException) {
            try {
                this.launch(IntentSenderRequest.Builder(exception.resolution).build())
            } catch (sendEx: IntentSender.SendIntentException) {
                Timber.e(exception, "Error requesting location enabling")
            }
        }
    }
        .addOnSuccessListener {
            locationAlreadyEnable?.invoke()
        }
}

fun ActivityResultLauncher<Intent>.requestEnableBluetooth() {
    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    this.launch(intent)
}

fun Context.checkSelfPermissionByName(
    permissionName: String,
    onRequestPermission: (permissionName : String) -> Unit = {},
    showPermissionRationale: () -> Unit = {},
    onGranted: () -> Unit = {}
) {
    if (ContextCompat.checkSelfPermission(
            this,
            permissionName
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this as ComponentActivity,
                permissionName
            )
        ) {
            showPermissionRationale()
        } else {
            onRequestPermission(permissionName)
        }
    } else {
        onGranted()
    }
}

fun Context.checkBluetoothSelfPermission(
    onRequestPermission: () -> Unit = {},
    showPermissionRationale: () -> Unit = {},
    onGranted: () -> Unit = {}
) {
    if (
        ContextCompat.checkSelfPermission(
            this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(
            this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
    ) {
        if (
            ActivityCompat.shouldShowRequestPermissionRationale(
                this as ComponentActivity, Manifest.permission.BLUETOOTH_SCAN)
            && ActivityCompat.shouldShowRequestPermissionRationale(
                this as ComponentActivity, Manifest.permission.BLUETOOTH_CONNECT)
        ) {
            showPermissionRationale()
        } else {
            onRequestPermission()
        }
    } else {
        onGranted()
    }
}

fun checkPermissions(activity: Activity) {
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

    val permissionsList = mutableListOf<String>()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionsList.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    check(permissionsList)
}

private fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
    ActivityCompat.requestPermissions(activity, permissions, requestCode)
}
