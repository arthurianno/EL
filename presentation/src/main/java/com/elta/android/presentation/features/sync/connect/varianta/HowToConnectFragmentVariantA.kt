package com.elta.android.presentation.features.sync.connect

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ExperimentalCameraProviderConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.common.PermissionEvent
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.features.sync.connect.DmcConnectionIntroScreen
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.viewmodel.HowToConnectViewModelVariantA
import com.elta.android.presentation.features.sync.control.enableLocation
import com.elta.android.presentation.utils.bundle
import com.elta.android.presentation.utils.openSettingsIntent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

private val requiredPermissions = listOf(
    Manifest.permission.CAMERA,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.BLUETOOTH_CONNECT
)

// fixme Variant A : improved_enabling_location
@ExperimentalCameraProviderConfiguration
class HowToConnectFragmentVariantA : BaseComposeFragment<HowToConnectViewModelVariantA>() {
    companion object {
        fun newInstance(isOnBoarding: Boolean) = HowToConnectFragmentVariantA().apply {
            arguments = bundle(IS_ON_BOARDING_ARGUMENT_NAME to isOnBoarding)
        }
    }

    override val viewModel: HowToConnectViewModelVariantA by viewModels { viewModelFactory }

    override fun HowToConnectViewModelVariantA.init() {
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
    override fun Dialogs(viewModel: HowToConnectViewModelVariantA) {
        BaseDialog(widgetModel = viewModel.cameraPermissionDialog)
        BaseDialog(widgetModel = viewModel.locationPermissionDialog)
        BaseDialog(widgetModel = viewModel.bluetoothPermissionDialog)
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    override fun Content(viewModel: HowToConnectViewModelVariantA) {
        val permissions =
            rememberMultiplePermissionsState(permissions = requiredPermissions)
        val event = viewModel.event.collectAsState(initial = null).value
        LaunchedEffect(key1 = event) {
            when (event) {
                is PermissionEvent.RequestPermissions -> permissions.launchMultiplePermissionRequest()
                is PermissionEvent.OpenSettings -> openSettingsIntent(requireContext())
                is PermissionEvent.Bluetooth.RequestEnable -> requestEnableBluetooth()
                is PermissionEvent.Bluetooth.OnAllow -> {
                    viewModel sendAction ConnectAction.OpenConnectingScreen
                }
                is PermissionEvent.RequestEnableLocation ->
                    enableLocation(this@HowToConnectFragmentVariantA) {
                        requestEnableBluetooth()
                    }
            }
        }
        DmcConnectionIntroScreen(
            onBack = { viewModel sendAction AppAction.BackPressure },
            onScan = { viewModel sendAction ConnectAction.CheckPermissionsState(permissions.permissions) }
        )
    }

    private fun requestEnableBluetooth() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        resultLauncher.launch(intent)
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val connectAction = when (result.resultCode) {
            Activity.RESULT_OK -> ConnectAction.Complete
            else -> ConnectAction.RepeatConnect
        }
        viewModel sendAction connectAction
    }
}
