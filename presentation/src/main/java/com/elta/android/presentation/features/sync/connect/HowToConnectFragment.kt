package com.elta.android.presentation.features.sync.connect

import android.Manifest
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.lifecycle.ExperimentalCameraProviderConfiguration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBar
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.features.sync.connect.model.howtoconnect.HowToConnectAction
import com.elta.android.presentation.features.sync.connect.model.howtoconnect.HowToConnectEvent
import com.elta.android.presentation.features.sync.connect.viewmodel.HowToConnectViewModel
import com.elta.android.presentation.features.sync.connect.widgets.BluetoothString
import com.elta.android.presentation.features.sync.connect.widgets.MainImage
import com.elta.android.presentation.features.sync.connect.widgets.TextNumericItem
import com.elta.android.presentation.features.sync.control.checkBluetoothSelfPermission
import com.elta.android.presentation.features.sync.control.checkSelfPermissionByName
import com.elta.android.presentation.features.sync.control.requestEnableBluetooth
import com.elta.android.presentation.features.sync.control.requestEnableLocation
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.utils.bundle
import com.elta.android.presentation.utils.openSettingsIntent
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.runtime.collectAsState

@ExperimentalCameraProviderConfiguration
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
        GetLocalProperties { dimens, _, colors, _, _ ->
            // Показываем контент только когда всё готово (данные + картинка проверена)
            AnimatedVisibility(
                visible = viewModel.state.collectAsState().value.isContentReady,
                enter = fadeIn()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = colors.white)
                        .systemBarsPadding()
                ) {
                    TopAppBar(viewModel)
                    MainImage(
                        imageUrl = viewModel.state.collectAsState().value.screenConfig?.backgroundImageUrl,
                        imageId = R.drawable.img_dmc_connect)
                    Info(viewModel)
                    VSpacer(dimens.bigDim)
                    DownButton(
                        widgetModel = viewModel.downButton,
                        onClickAction = HowToConnectAction.OnConnectButtonClick
                    )
                }
            }
        }
    }

    @Composable
    private fun Info(viewModel: HowToConnectViewModel) {
        GetLocalProperties { dimens, _, _, _, _ ->
            Column(Modifier.padding(dimens.contentPadding)) {
                Title(viewModel)
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
    private fun Title(viewModel : HowToConnectViewModel) {
        GetLocalProperties { _, _, _, _, types ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = viewModel.state.value.screenConfig?.title ?: stringResource(id = R.string.sync_how_to_connect_title),
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
