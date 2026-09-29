package com.elta.android.presentation.features.sync.connect

import android.Manifest
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.features.sync.connect.model.connecting.ConnectingStageType
import com.elta.android.presentation.features.sync.connect.model.connecting.ConnectingViewAction
import com.elta.android.presentation.features.sync.connect.model.connecting.ConnectingViewEvent
import com.elta.android.presentation.features.sync.connect.viewmodel.ConnectingViewModel
import com.elta.android.presentation.features.sync.control.checkSelfPermissionByName
import com.elta.android.presentation.features.sync.control.requestEnableBluetooth
import com.elta.android.presentation.features.sync.control.requestEnableLocation
import com.elta.android.presentation.utils.bundle
import com.elta.android.presentation.utils.openSettingsIntent
import kotlinx.coroutines.flow.collectLatest

class ConnectingFragment : BaseComposeFragment<ConnectingViewModel>() {
    companion object {
        fun newInstance(isOnBoarding: Boolean, pin: String, name: String) = ConnectingFragment().apply {
            arguments = bundle(
                IS_ON_BOARDING_ARGUMENT_NAME to isOnBoarding,
                PIN_ARGUMENT_NAME to pin,
                GLUCOMETER_NAME_ARGUMENT_NAME to name
            )
        }
    }

    override val viewModel: ConnectingViewModel by viewModels { viewModelFactory }

    override fun ConnectingViewModel.init() {
        exitDialogFromConnecting.initDialog(
            message = getString(R.string.sync_connection_exit_from_connecting_dialog_text),
            positiveButtonText = getString(R.string.yes_text),
            negativeButtonText = getString(R.string.cancel_text)
        )
        exitDialogFromSync.initDialog(
            message = getString(R.string.sync_connection_exit_from_sync_dialog_text),
            positiveButtonText = getString(R.string.yes_text),
            negativeButtonText = getString(R.string.cancel_text)
        )
        warningNeedLocation.initDialog(
            message = getString(R.string.device_need_location_dialog_message),
            positiveButtonText = getString(R.string.device_need_location_dialog_positive_button),
            negativeButtonText = getString(R.string.device_need_location_dialog_negative_button)
        )
        locationPermissionDialog.initDialog(
            title = getString(R.string.settings_dialog_title),
            message = getString(R.string.location_dialog_message),
            positiveButtonText = getString(R.string.settings_dialog_positive),
            negativeButtonText = getString(R.string.settings_dialog_negative)
        )
    }

    @Composable
    override fun Dialogs(viewModel: ConnectingViewModel) {
        BaseDialog(widgetModel = viewModel.exitDialogFromConnecting)
        BaseDialog(widgetModel = viewModel.exitDialogFromSync)
        BaseDialog(widgetModel = viewModel.warningNeedLocation)
        BaseDialog(widgetModel = viewModel.locationPermissionDialog)
    }

    @Composable
    override fun Content(viewModel: ConnectingViewModel) {
        val context = LocalContext.current
        val stage = viewModel.state.collectAsState().value.stageType

        LaunchedEffect(Unit) {
            viewModel.event.collectLatest { event ->
                when (event) {
                    is ConnectingViewEvent.OpenSettings -> openSettingsIntent(requireContext())
                    is ConnectingViewEvent.EnableBluetooth -> bluetoothResultLauncher.requestEnableBluetooth()
                    is ConnectingViewEvent.Location.RequestPermission -> {
                        context.checkSelfPermissionByName(
                            permissionName = Manifest.permission.ACCESS_FINE_LOCATION,
                            onRequestPermission = { locationPermissionLauncher.launch(it) },
                            showPermissionRationale = {
                                viewModel sendAction ConnectingViewAction.Location.ShowPermissionRationale
                            },
                            onGranted = {
                                viewModel sendAction ConnectingViewAction.Location.AllowPermission
                            }
                        )
                    }
                    is ConnectingViewEvent.Location.Enable ->
                        locationEnableResultLauncher.requestEnableLocation(context) {
                            viewModel sendAction ConnectingViewAction.Location.Enable
                        }
                    else -> Unit
                }
            }
        }

        DmcConnectionProgressScreen(
            stage = stage,
            onBack = { viewModel sendAction AppAction.BackPressure },
            onAction = {
                val action = when (stage) {
                    ConnectingStageType.Complete -> ConnectingViewAction.ClickCompleteButton
                    ConnectingStageType.DeviceNotFound -> ConnectingViewAction.ClickSearchButton
                    ConnectingStageType.ErrorConnect -> ConnectingViewAction.ClickRepeatButton
                    ConnectingStageType.ErrorSync -> ConnectingViewAction.ClickRepeatSyncButton
                    else -> null
                }
                action?.let { viewModel sendAction it }
            }
        )
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel sendAction if (isGranted) ConnectingViewAction.Location.AllowPermission
        else ConnectingViewAction.Location.DeniedPermission
    }

    private val locationEnableResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel sendAction ConnectingViewAction.Location.Enable
        }
    }

    private val bluetoothResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel sendAction if (result.resultCode == Activity.RESULT_OK) {
            ConnectingViewAction.Bluetooth.Enable
        } else {
            ConnectingViewAction.Bluetooth.Reject
        }
    }
}
