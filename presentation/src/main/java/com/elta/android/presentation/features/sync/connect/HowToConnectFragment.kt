package com.elta.android.presentation.features.sync.connect

import android.Manifest
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ExperimentalCameraProviderConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.features.sync.connect.model.howtoconnect.HowToConnectAction
import com.elta.android.presentation.features.sync.connect.model.howtoconnect.HowToConnectEvent
import com.elta.android.presentation.features.sync.connect.viewmodel.HowToConnectViewModel
import com.elta.android.presentation.features.sync.control.checkBluetoothSelfPermission
import com.elta.android.presentation.features.sync.control.checkSelfPermissionByName
import com.elta.android.presentation.features.sync.control.requestEnableBluetooth
import com.elta.android.presentation.features.sync.control.requestEnableLocation
import com.elta.android.presentation.utils.bundle
import com.elta.android.presentation.utils.openSettingsIntent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.flow.collectLatest

@ExperimentalCameraProviderConfiguration
class HowToConnectFragment : BaseComposeFragment<HowToConnectViewModel>() {
    companion object {
        fun newInstance(isOnBoarding: Boolean) = HowToConnectFragment().apply {
            arguments = bundle(IS_ON_BOARDING_ARGUMENT_NAME to isOnBoarding)
        }
    }

    override val viewModel: HowToConnectViewModel by viewModels { viewModelFactory }

    override fun HowToConnectViewModel.init() {
        cameraPermissionDialog.initDialog(
            title = getString(R.string.settings_dialog_title),
            message = getString(R.string.camera_dialog_message),
            positiveButtonText = getString(R.string.settings_dialog_positive),
            negativeButtonText = getString(R.string.settings_dialog_negative)
        )

        locationPermissionDialog.initDialog(
            title = getString(R.string.settings_dialog_title),
            message = getString(R.string.location_dialog_message),
            positiveButtonText = getString(R.string.settings_dialog_positive),
            negativeButtonText = getString(R.string.settings_dialog_negative)
        )

        bluetoothPermissionDialog.initDialog(
            title = getString(R.string.settings_dialog_title),
            message = getString(R.string.bluetooth_dialog_message),
            positiveButtonText = getString(R.string.settings_dialog_positive),
            negativeButtonText = getString(R.string.settings_dialog_negative)
        )
    }

    @Composable
    override fun Dialogs(viewModel: HowToConnectViewModel) {
        BaseDialog(widgetModel = viewModel.cameraPermissionDialog)
        BaseDialog(widgetModel = viewModel.locationPermissionDialog)
        BaseDialog(widgetModel = viewModel.bluetoothPermissionDialog)
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    override fun Content(viewModel: HowToConnectViewModel) {
        val context = LocalContext.current
        LaunchedEffect(key1 = Unit) {
            viewModel.event.collectLatest {
                when (it) {
                    is HowToConnectEvent.OpenSettings -> openSettingsIntent(requireContext())
                    is HowToConnectEvent.Bluetooth.Enable -> bluetoothResultLauncher.requestEnableBluetooth()
                    is HowToConnectEvent.RequestCameraPermission ->
                        context.checkSelfPermissionByName(
                            permissionName = Manifest.permission.CAMERA,
                            onRequestPermission = { permissionName ->
                                viewModel sendAction HowToConnectAction.Camera.AppearPermission
                                cameraPermissionLauncher.launch(permissionName)
                            },
                            showPermissionRationale = {
                                viewModel sendAction HowToConnectAction.Camera.ShowPermissionRationale
                            },
                            onGranted = {
                                viewModel sendAction HowToConnectAction.Camera.AllowPermission(isAlreadyGranted = true)
                            }
                        )
                    is HowToConnectEvent.Bluetooth.RequestPermission ->
                        context.checkBluetoothSelfPermission(
                            onRequestPermission = {
                                viewModel sendAction HowToConnectAction.Bluetooth.AppearPermission
                                bluetoothPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.BLUETOOTH_SCAN,
                                        Manifest.permission.BLUETOOTH_CONNECT
                                    )
                                )
                            },
                            showPermissionRationale = {
                                viewModel sendAction  HowToConnectAction.Bluetooth.ShowPermissionRationale
                            },
                            onGranted = {
                                viewModel sendAction HowToConnectAction.Bluetooth.AllowPermission(isAlreadyGranted = true)
                            }
                        )
                    is HowToConnectEvent.Location.RequestPermission ->
                        context.checkSelfPermissionByName(
                            permissionName = Manifest.permission.ACCESS_FINE_LOCATION,
                            onRequestPermission = { permissionName ->
                                viewModel sendAction HowToConnectAction.Location.AppearPermission
                                locationPermissionLauncher.launch(permissionName)
                            },
                            showPermissionRationale = {
                                viewModel sendAction HowToConnectAction.Location.ShowPermissionRationale
                            },
                            onGranted = {
                                viewModel sendAction HowToConnectAction.Location.AllowPermission(isAlreadyGranted = true)
                            },
                        )

                    is HowToConnectEvent.Location.Enable ->
                        locationEnableResultLauncher.requestEnableLocation(context) {
                            viewModel sendAction HowToConnectAction.Location.Enabled
                        }
                }
            }
        }
        DmcConnectionIntroScreen(
            onBack = { viewModel sendAction AppAction.BackPressure },
            onScan = { viewModel sendAction HowToConnectAction.OnConnectButtonClick }
        )
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel sendAction HowToConnectAction.Camera.AllowPermission(isAlreadyGranted = false)
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel sendAction HowToConnectAction.Location.AllowPermission(isAlreadyGranted = false)
        }
    }

    private val locationEnableResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                viewModel sendAction HowToConnectAction.Location.Enabled
            }
        }
    }

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        if (permissionsMap.all { it.value }) {
            viewModel sendAction HowToConnectAction.Bluetooth.AllowPermission(isAlreadyGranted = false)
        }
    }

    private val bluetoothResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val connectAction = when (result.resultCode) {
            Activity.RESULT_OK -> HowToConnectAction.Bluetooth.Enabled
            else -> HowToConnectAction.Bluetooth.Rejected
        }
        viewModel sendAction connectAction
    }
}
