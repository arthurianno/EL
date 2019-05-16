package com.elta.android.presentation.features.sync.connect.ui

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.view.View
import com.elta.android.presentation.R
import com.elta.android.presentation.core.ui.fragment.BaseListFragment
import com.elta.android.presentation.core.ui.system_ui.LightStatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.features.sync.connect.pm.ConnectDevicePm
import com.elta.android.presentation.features.sync.pin.ui.PinDialogFragment
import com.elta.android.presentation.utils.makeSnackBar
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.SettingsClient
import com.jakewharton.rxbinding2.view.clicks
import com.nullgr.core.intents.launchForResult
import com.nullgr.core.ui.extensions.children
import com.nullgr.core.ui.extensions.hide
import com.nullgr.core.ui.extensions.toggleView
import com.nullgr.core.ui.fragments.showDialog
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.fragment_sync_connect.*
import kotlinx.android.synthetic.main.layout_sync_state_device_found.*
import kotlinx.android.synthetic.main.layout_sync_state_sync_completed.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import timber.log.Timber

class ConnectDeviceFragment : BaseListFragment<ConnectDevicePm>() {

    override val screenLayout: Int = R.layout.fragment_sync_connect
    override val classToken: Class<ConnectDevicePm> = ConnectDevicePm::class.java
    override val statusBarConfigProvider: StatusBarConfigProvider = LightStatusBarConfigProvider

    private val rxPermissions by lazy { RxPermissions(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeButtonView.hide()
        menuButtonView.text = getString(R.string.sync_connect_menu_button_text)
    }

    override fun onBindPresentationModel(pm: ConnectDevicePm) {
        super.onBindPresentationModel(pm)
        bindProgressDialog(pm)
        menuButtonView.clicks().bindTo(pm.skipAction)
        actionButtonView.clicks().bindTo(pm.connectDeviceAction)
        toAppButtonView.clicks().bindTo(pm.toAppAction)
        pm.connectDeviceEnabledState.bindTo(actionButtonView::setEnabled)
        pm.state.bindTo { state ->
            syncStateContainerView.children().forEach { view ->
                view.toggleView(state.getId() == view.id)
            }
        }

        pm.retrySearchControl.bindTo { data, sc ->
            makeSnackBar(checkNotNull(view), data)
                .setDuration(Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(ContextCompat.getColor(checkNotNull(context), R.color.shade_blue))
                .setAction(data.button) { sc.sendResult() }
        }

        pm.retryPinControl.bindTo { data, sc ->
            makeSnackBar(checkNotNull(view), data)
                .setDuration(Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(ContextCompat.getColor(checkNotNull(context), R.color.shade_blue))
                .setAction(data.button) { sc.sendResult() }
        }

        pm.retrySyncControl.bindTo { data, sc ->
            makeSnackBar(checkNotNull(view), data)
                .setDuration(Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(ContextCompat.getColor(checkNotNull(context), R.color.shade_blue))
                .setAction(data.button) { sc.sendResult() }
        }

        pm.requestEnableBluetoothCommand.observable
            .bindTo {
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    .launchForResult(checkNotNull(activity), REQUEST_CODE_ENABLE_BLUETOOTH)
            }
        pm.requestLocationPermissionsCommand.observable
            .switchMap {
                rxPermissions.request(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    .filter { it }
                    .map { Unit }
            }
            .bindTo(pm.locationPermissionsGrantedAction)
        pm.requestEnableLocationCommand.observable
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ENABLE_LOCATION && resultCode == Activity.RESULT_OK) {
            presentationModel.locationEnabledAction.consumer.accept(Unit)
        }

        if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH && resultCode == Activity.RESULT_OK) {
            presentationModel.bluetoothEnabledAction.consumer.accept(Unit)
        }
    }

    private inline fun ConnectDevicePm.ViewState.getId() =
        when (this) {
            ConnectDevicePm.ViewState.SEARCH -> R.id.stateConnectView
            ConnectDevicePm.ViewState.FOUND -> R.id.stateDeviceFoundView
            ConnectDevicePm.ViewState.CONNECTED -> R.id.stateConnectedView
            ConnectDevicePm.ViewState.SYNC_COMPLETED -> R.id.stateSyncCompletedView
        }

    companion object {
        private const val REQUEST_CODE_ENABLE_LOCATION = 145
        private const val REQUEST_CODE_ENABLE_BLUETOOTH = 146

        fun newInstance(): ConnectDeviceFragment = ConnectDeviceFragment()
    }
}
