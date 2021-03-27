package com.elta.android.presentation.features.bluetooth.ui

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import com.elta.android.common.utils.log
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.bluetooth.pm.BluetoothPm
import com.elta.android.presentation.features.sync.pin.ui.PinDialogFragment
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import com.nullgr.core.intents.launchForResult
import com.nullgr.core.ui.fragments.showDialog
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
        bindClicks(pm)
        bindViews(pm)
        observeCommands(pm)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("onActivityResult")
        if (requestCode == REQUEST_CODE_ENABLE_LOCATION && resultCode == Activity.RESULT_OK) {
            presentationModel.locationEnabledAction.consumer.accept(Unit)
            Timber.d("Location enabled")
        }

        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH && resultCode == Activity.RESULT_OK) {
            presentationModel.bluetoothEnabledAction.consumer.accept(Unit)
            Timber.d("Bluetooth enabled")
        }
    }

    private fun bindViews(pm: BluetoothPm) {
        pm.updateEnabledState.bindTo { updateFirmwareButtonView.isEnabled = it }
        pm.downloadEnabledState.bindTo { downloadFirmwareButtonView.isEnabled = it }
        pm.pinEnabledState.bindTo { setPinButtonView.isEnabled = it }
        pm.pinInputControl.bindTo(commandInputView)
        pm.logState.bindTo(logView.text())
    }

    private fun observeCommands(pm: BluetoothPm) {
        pm.requestEnableBluetoothCommand.observable
            .log("Command", "enable bluetooth")
            .bindTo {
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    .launchForResult(checkNotNull(activity), REQUEST_CODE_ENABLE_BLUETOOTH)
            }
        pm.requestLocationPermissionsCommand.observable
            .log("Command", "request permissions")
            .switchMap {
                rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
                    .filter { it }
                    .map { Unit }
            }
            .bindTo(pm.locationPermissionsGrantedAction)
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
                        task.getResult(ApiException::class.java)
                    } catch (e: ApiException) {
                        when (e.statusCode) {
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                                try {
                                    (e as? ResolvableApiException)?.startResolutionForResult(
                                        checkNotNull(activity),
                                        REQUEST_CODE_ENABLE_LOCATION
                                    )
                                } catch (e1: IntentSender.SendIntentException) {
                                    Timber.e(e1)
                                }
                        }
                    }
                }
            }
        pm.openPinCodeDialogCommand.bindTo {
            childFragmentManager.showDialog(PinDialogFragment.newInstance(it))
        }
    }

    private fun bindClicks(pm: BluetoothPm) {
        getInfoButtonView.clicks().bindTo(pm.getInfoAction)
        getEventsButtonView.clicks().bindTo(pm.getEventsAction)
        setPinButtonView.clicks().bindTo(pm.setPinAction)
        checkFirmwareButtonView.clicks().bindTo(pm.checkFirmwareAction)
        downloadFirmwareButtonView.clicks().bindTo(pm.downloadFirmwareAction)
        updateFirmwareButtonView.clicks().bindTo(pm.updateFirmwareAction)
    }

    companion object {
        private const val REQUEST_CODE_ENABLE_LOCATION = 145
        private const val REQUEST_CODE_ENABLE_BLUETOOTH = 146

        fun newInstance(): BluetoothFragment {
            return BluetoothFragment().apply {
                arguments = Bundle().apply {
                }
            }
        }
    }
}
