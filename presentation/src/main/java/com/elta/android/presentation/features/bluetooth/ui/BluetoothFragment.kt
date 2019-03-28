package com.elta.android.presentation.features.bluetooth.ui

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.elta.android.common.utils.log
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.bluetooth.pm.BluetoothPm
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import com.nullgr.core.intents.launchForResult
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_bluetooth.*
import timber.log.Timber

class BluetoothFragment : BaseListFragment<BluetoothPm>() {

    override val screenLayout: Int = R.layout.fragment_bluetooth
    override val classToken: Class<BluetoothPm> = BluetoothPm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    private val rxPermissions by lazy { RxPermissions(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logView.movementMethod = ScrollingMovementMethod()
    }

    override fun onBindPresentationModel(pm: BluetoothPm) {
        super.onBindPresentationModel(pm)
        writeButtonView.clicks().bindTo(pm.writeAction)
        connectButtonView.clicks().bindTo(pm.connectAction)
        dfuButtonView.clicks().bindTo(pm.dfuAction)
        pm.commandInputControl.bindTo(commandInputView)
        pm.logState.bindTo(logView.text())
        pm.requestEnableBluetoothCommand.observable
            .log("Command", "enable bluetooth")
            .bindTo {
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).launchForResult(checkNotNull(activity), 124)
            }
        pm.requestLocationPermissionsCommand.observable
            .log("Command", "permission")
            .bindTo {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION), 125)
            }
        pm.requestEnableLocationCommand.observable
            .log("Command", "enable location")
            .bindTo {
                val result = SettingsClient(checkNotNull(context))
                    .checkLocationSettings(
                        LocationSettingsRequest.Builder()
                            .addLocationRequest(LocationRequest.create())
                            .setNeedBle(true)
                            .build()
                    )
                result.addOnCompleteListener { task ->
                    try {
                        val response = task.getResult(ApiException::class.java)
                        pm.startScanAction.consumer.accept(Unit)
                    } catch (e: ApiException) {
                        Timber.e(e)
                        when (e.statusCode) {
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                                try {
                                    (e as? ResolvableApiException)?.startResolutionForResult(checkNotNull(activity), 123)
                                } catch (e1: IntentSender.SendIntentException) {
                                    Timber.e(e1)
                                }
                            }
                        }
                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("onActivityResult")
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            presentationModel.startScanAction.consumer.accept(Unit)
            Timber.d("Location enabled")
        }

        if (requestCode == 124 && resultCode == Activity.RESULT_OK) {
            presentationModel.startScanAction.consumer.accept(Unit)
            Timber.d("Bluetooth enabled")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 125) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                presentationModel.startScanAction.consumer.accept(Unit)
            }
            return
        }
    }

    companion object {
        fun newInstance(): BluetoothFragment {
            return BluetoothFragment().apply {
                arguments = Bundle().apply {
                }
            }
        }
    }
}
