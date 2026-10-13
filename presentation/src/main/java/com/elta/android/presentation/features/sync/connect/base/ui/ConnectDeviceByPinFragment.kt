package com.elta.android.presentation.features.sync.connect.base.ui

import android.content.Intent
import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import com.elta.android.presentation.core.ui.dialog.createDialog
import com.elta.android.presentation.features.sync.connect.pin.PinConnectionFragment
import com.elta.android.presentation.features.sync.connect.base.pm.ConnectDevicePm
import com.elta.android.presentation.features.sync.control.bindTo
import com.elta.android.presentation.features.sync.control.resolveResults
import com.elta.android.presentation.features.sync.control.checkBluetoothSelfPermission
import com.elta.android.presentation.features.sync.control.checkSelfPermissionByName
import com.elta.android.presentation.utils.openSettingsIntent
import com.tbruyelle.rxpermissions2.RxPermissions
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo

abstract class ConnectDeviceByPinFragment<T : ConnectDevicePm> : PinConnectionFragment<T>() {

    private val rxPermissions: RxPermissions by lazy { RxPermissions(requireActivity()) }

    override fun onBindPresentationModel(pm: T) {
        super.onBindPresentationModel(pm)
        // Откладываем инициализацию RxPermissions чтобы избежать конфликта с FragmentManager
        binding.root.post {
            if (isAdded && !isStateSaved) {
                pm.btControl.bindTo(compositeUnbind, rxPermissions, this@ConnectDeviceByPinFragment)
            }
        }

        pm.settingsDialog.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.settingsIsVisible.bindTo {
            if (it) {
                openSettingsIntent(requireContext())
                pm.openSettingsCloseAction.consumer.accept(Unit)
            }
        }
        pm.deviceAlreadyConnectedDialog.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.deviceNeedLocationDialog.bindTo { data, dc -> createDialog(this, dc, data) }
        pm.checkBluetoothPermissionCommand.bindTo {
            context?.checkBluetoothSelfPermission(
                onRequestPermission = {
                    bluetoothPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT
                        )
                    )
                },
                showPermissionRationale = {
                    pm.showBluetoothPermissionRationaleAction.consumer.accept(Unit)
                },
                onGranted = {
                    pm.onBluetoothPermissionGrantedAction.consumer.accept(Unit)
                }
            )
        }
        pm.checkLocationPermissionCommand.bindTo {
            context?.checkSelfPermissionByName(
                permissionName = Manifest.permission.ACCESS_FINE_LOCATION,
                onRequestPermission = { permissionName ->
                    locationPermissionLauncher.launch(permissionName)
                },
                showPermissionRationale = {
                    pm.showLocationPermissionRationaleAction.consumer.accept(Unit)
                },
                onGranted = {
                    pm.onLocationPermissionGrantedAction.consumer.accept(Unit)
                }
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presentationModel.btControl.resolveResults(requestCode, resultCode)
    }

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        presentationModel.receivedBluetoothPermissionGrantedAction.consumer.accept(permissionsMap.all { it.value })
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        presentationModel.receivedLocationPermissionGrantedAction.consumer.accept(isGranted)
    }

}
