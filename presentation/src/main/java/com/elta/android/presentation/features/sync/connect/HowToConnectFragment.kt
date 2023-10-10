package com.elta.android.presentation.features.sync.connect

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.common.PermissionEvent
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.viewmodel.HowToConnectViewModel
import com.elta.android.presentation.features.sync.connect.widgets.BluetoothString
import com.elta.android.presentation.features.sync.connect.widgets.MainImage
import com.elta.android.presentation.features.sync.connect.widgets.TextNumericItem
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.utils.bundle
import com.elta.android.presentation.utils.openSettingsIntent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

private val requiredPermissions = listOf(
    Manifest.permission.CAMERA,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.BLUETOOTH,
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.BLUETOOTH_CONNECT
)

class HowToConnectFragment : BaseComposeFragment<HowToConnectViewModel>() {
    companion object {
        fun newInstance(isOnBoarding: Boolean) = HowToConnectFragment().apply {
            arguments = bundle(IS_ON_BOARDING_ARGUMENT_NAME to isOnBoarding)
        }
    }

    override val viewModel: HowToConnectViewModel by viewModels { viewModelFactory }

    override fun HowToConnectViewModel.init() {
        appTopBar.setStartIconAction(AppAction.BackPressure)
        downButton.setText(getString(R.string.sync_how_to_connect_button))
        downButton.setEnableState(true)
        downButton.visibilityState(true)

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
        val permissions =
            rememberMultiplePermissionsState(permissions = requiredPermissions)
        val event = viewModel.event.collectAsState(initial = null).value
        LaunchedEffect(key1 = event) {
            if (event is PermissionEvent.Camera ||
                event is PermissionEvent.FineLocation ||
                event is PermissionEvent.Bluetooth
            ) {
                permissions.launchMultiplePermissionRequest()
            }
            when (event) {
                is PermissionEvent.Camera -> permissions.launchMultiplePermissionRequest()
                is PermissionEvent.FineLocation -> permissions.launchMultiplePermissionRequest()
                is PermissionEvent.OpenSettings -> openSettingsIntent(requireContext())
                is PermissionEvent.Bluetooth.RequestEnable -> {
                    permissions.launchMultiplePermissionRequest()
                    requestEnableBluetooth()
                }
                is PermissionEvent.Bluetooth.OnAllow -> {
                    viewModel sendAction ConnectAction.OpenConnectingScreen(permissions.permissions)
                }
            }
        }
        GetLocalProperties { dimens, _, _, _, _ ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                TopAppBar(viewModel)
                MainImage(imageId = R.drawable.img_dmc_connect)
                Info()
                VSpacer(dimens.bigDim)
                DownButton(
                    widgetModel = viewModel.downButton,
                    onClickAction = ConnectAction.OpenConnectingScreen(permissions.permissions)
                )
            }
        }
    }

    @Composable
    private fun Info() {
        GetLocalProperties { dimens, _, _, _, _ ->
            Column(Modifier.padding(dimens.contentPadding)) {
                Title()
                VSpacer(dimens.halfMediumDim)
                TextNumericItem(
                    number = R.string.list_numbering_1_dot,
                    text = R.string.how_to_connect_description_check_ble
                )
                VSpacerSmall()
                BluetoothString()
                VSpacerMedium()
                TextNumericItem(
                    number = R.string.list_numbering_2_dot,
                    text = R.string.sync_how_to_connect_text_2
                )
            }
        }
    }

    @Composable
    private fun Title() {
        GetLocalProperties { _, _, _, _, types ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.sync_how_to_connect_title),
                    style = types.h1
                )
                HSpacerSmall()
                Image(
                    painter = painterResource(id = R.drawable.ic_how_to_connect),
                    contentDescription = null
                )
            }
        }
    }

    @Composable
    private fun TopAppBar(viewModel: HowToConnectViewModel) {
        GetLocalProperties { _, _, colors, _, _ ->
            BaseAppTopBar(
                widgetModel = viewModel.appTopBar,
                backgroundColor = colors.white,
                startIcon = R.drawable.ic_back,
                startIconColor = colors.blackBlue
            )
        }
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
