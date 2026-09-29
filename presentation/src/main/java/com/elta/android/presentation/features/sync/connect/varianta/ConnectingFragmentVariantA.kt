package com.elta.android.presentation.features.sync.connect

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.connecting.ConnectingStageType
import com.elta.android.presentation.features.sync.connect.viewmodel.ConnectingViewModelVariantA
import com.elta.android.presentation.utils.bundle

class ConnectingFragmentVariantA : BaseComposeFragment<ConnectingViewModelVariantA>() {
    companion object {
        fun newInstance(isOnBoarding: Boolean, pin: String, name: String) = ConnectingFragmentVariantA().apply {
            arguments = bundle(
                IS_ON_BOARDING_ARGUMENT_NAME to isOnBoarding,
                PIN_ARGUMENT_NAME to pin,
                GLUCOMETER_NAME_ARGUMENT_NAME to name
            )
        }
    }

    override val viewModel: ConnectingViewModelVariantA by viewModels { viewModelFactory }

    override fun ConnectingViewModelVariantA.init() {
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
    }

    @Composable
    override fun Dialogs(viewModel: ConnectingViewModelVariantA) {
        BaseDialog(widgetModel = viewModel.exitDialogFromConnecting)
        BaseDialog(widgetModel = viewModel.exitDialogFromSync)
    }

    @Composable
    override fun Content(viewModel: ConnectingViewModelVariantA) {
        val state = viewModel.state.collectAsState().value
        LaunchedEffect(state.requestBluetoothActivation) {
            if (state.requestBluetoothActivation) {
                requestEnableBluetooth()
            }
        }

        DmcConnectionProgressScreen(
            stage = state.stageType,
            onBack = { viewModel sendAction AppAction.BackPressure },
            onAction = {
                val action = when (state.stageType) {
                    ConnectingStageType.Complete -> ConnectAction.Complete
                    ConnectingStageType.DeviceNotFound -> ConnectAction.RepeatSearch
                    ConnectingStageType.ErrorConnect -> ConnectAction.RepeatConnect
                    ConnectingStageType.ErrorSync -> ConnectAction.RepeatSync
                    else -> null
                }
                action?.let { viewModel sendAction it }
            }
        )
    }

    private fun requestEnableBluetooth() {
        resultLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel sendAction if (result.resultCode == Activity.RESULT_OK) {
            ConnectAction.RepeatSearch
        } else {
            ConnectAction.ScannerError
        }
    }
}
