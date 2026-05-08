package com.elta.android.presentation.features.sync.connect

import android.Manifest
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.BaseComposeFragment
import com.elta.android.presentation.core.compose.widgets.HSpacerSmall
import com.elta.android.presentation.core.compose.widgets.VSpacer
import com.elta.android.presentation.core.compose.widgets.VSpacerMedium
import com.elta.android.presentation.core.compose.widgets.VSpacerSmall
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.core.compose.widgets.snackbar.BaseSnackBar
import com.elta.android.presentation.core.ui.fragment.addOnBackPressedCallback
import com.elta.android.presentation.features.sync.connect.model.connecting.ConnectingStageType
import com.elta.android.presentation.features.sync.connect.model.connecting.ConnectingViewAction
import com.elta.android.presentation.features.sync.connect.model.connecting.ConnectingViewEvent
import com.elta.android.presentation.features.sync.connect.viewmodel.ConnectingViewModel
import com.elta.android.presentation.features.sync.connect.widgets.AppTopBar
import com.elta.android.presentation.features.sync.connect.widgets.HelpBottomSheet
import com.elta.android.presentation.features.sync.connect.widgets.MainImage
import com.elta.android.presentation.features.sync.connect.widgets.TextNumericItem
import com.elta.android.presentation.features.sync.control.checkSelfPermissionByName
import com.elta.android.presentation.features.sync.control.requestEnableBluetooth
import com.elta.android.presentation.features.sync.control.requestEnableLocation
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.utils.bundle
import com.elta.android.presentation.utils.openSettingsIntent
import kotlinx.coroutines.flow.collectLatest

class ConnectingFragment : BaseComposeFragment<ConnectingViewModel>() {
    companion object {
        fun newInstance(
            isOnBoarding: Boolean,
            pin: String,
            name: String
        ) = ConnectingFragment().apply {
            arguments = bundle(
                IS_ON_BOARDING_ARGUMENT_NAME to isOnBoarding,
                PIN_ARGUMENT_NAME to pin,
                GLUCOMETER_NAME_ARGUMENT_NAME to name
            )
        }
    }

    override val viewModel: ConnectingViewModel by viewModels { viewModelFactory }

    override fun ConnectingViewModel.init() {
        connectByPinButton.setText(getString(R.string.sync_connect_by_pin_boton_text))
        connectRepeatButton.setText(getString(R.string.repeat_connect_button_text))
        syncRepeatButton.setText(getString(R.string.repeat_sync_button_text))
        searchRepeatButton.setText(getString(R.string.repeat_search_button_text))
        completeButton.setText(getString(R.string.sync_state_action_sync_completed))
        appTopBar.setStartIconAction(AppAction.BackPressure)
        appTopBar.setEndIconAction(ConnectingViewAction.OpenHelp)
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback { viewModel.backClick() }
    }

    @Composable
    override fun Dialogs(viewModel: ConnectingViewModel) {
        BaseDialog(widgetModel = viewModel.exitDialogFromConnecting)
        BaseDialog(widgetModel = viewModel.exitDialogFromSync)
        BaseDialog(widgetModel = viewModel.warningNeedLocation)
        BaseDialog(widgetModel = viewModel.locationPermissionDialog)
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content(viewModel: ConnectingViewModel) {
        val context = LocalContext.current
        val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        val state = viewModel.state.collectAsState().value
        LaunchedEffect(key1 = Unit) {
            viewModel.event.collectLatest {
                when (it) {
                    is ConnectingViewEvent.ShowSheet -> sheetState.show()
                    is ConnectingViewEvent.HideSheet -> sheetState.hide()
                    is ConnectingViewEvent.OpenSettings -> openSettingsIntent(requireContext())
                    is ConnectingViewEvent.EnableBluetooth -> bluetoothResultLauncher.requestEnableBluetooth()
                    is ConnectingViewEvent.Location.RequestPermission -> {
                        context.checkSelfPermissionByName(
                            permissionName = Manifest.permission.ACCESS_FINE_LOCATION,
                            onRequestPermission = { permissionName ->
                                locationPermissionLauncher.launch(permissionName)
                            },
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
        GetLocalProperties { _, _, _, shapes, _ ->
            ModalBottomSheetLayout(
                sheetState = sheetState,
                sheetContent = {
                    HelpBottomSheet(
                        downButtonModel = viewModel.connectByPinButton,
                        connectAction = ConnectingViewAction.OnConnectClick,
                        closeOnClick = {
                            viewModel sendAction ConnectingViewAction.CloseHelp
                        }
                    )
                },
                sheetShape = shapes.sheet,
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Header(viewModel, state.stageType)
                    Footer(
                        viewModel = viewModel,
                        stageType = state.stageType,
                        glucometerName = state.glucometerName
                    )
                }
            }
        }
    }

    @Composable
    private fun ColumnScope.Header(
        viewModel: ConnectingViewModel,
        stageType: ConnectingStageType
    ) {
        val state = viewModel.state.collectAsState().value
        val isCompleted =
            stageType == ConnectingStageType.Complete || stageType == ConnectingStageType.Sync
        AppTopBar(
            appTopBarModel = viewModel.appTopBar,
            startIcon = R.drawable.ic_back.takeUnless { stageType == ConnectingStageType.Complete },
            endText = R.string.sync_connect_type_button_any_difficulties.takeUnless { isCompleted }
        )
        when (stageType) {
            ConnectingStageType.Complete -> {
                MainImage(
                    imageUrl = if (state.isSuccessImageReady) {
                        state.successfulSyncConfig?.backgroundImageUrl
                    } else null,
                    imageId = R.drawable.ic_connect_finish
                )
            }
            ConnectingStageType.Sync -> {
                MainImage(
                    imageUrl = if (state.isSuccessImageReady) {
                        state.connectingScreenConfig?.backgroundImageUrl
                    } else null,
                    imageId = R.drawable.ic_connect_dev
                )
            }
            ConnectingStageType.ErrorSync -> {
                MainImage(
                    imageUrl = if (state.isFailedImageReady) {
                        state.failedSyncConfig?.backgroundImageUrl
                    } else null,
                    imageId = R.drawable.ic_connect_dev
                )
            }

            ConnectingStageType.ErrorConnect -> {
                MainImage(
                    imageUrl = if (state.isFailedImageReady) {
                        state.failedSyncConfig?.backgroundImageUrl
                    } else null,
                    imageId = R.drawable.ic_connect_dev
                )
            }
            else -> {
                MainImage(
                    imageUrl = null,
                    imageId = R.drawable.ic_connect_dev
                )
            }
        }
    }

    @Composable
    private fun Footer(
        viewModel: ConnectingViewModel,
        stageType: ConnectingStageType,
        glucometerName: String,
    ) {
        val state = viewModel.state.collectAsState().value
        when (stageType) {
            ConnectingStageType.Connecting -> ConnectingFooter(state.connectingScreenConfig)
            ConnectingStageType.DeviceNotFound -> DeviceNotFoundFooter(viewModel)
            ConnectingStageType.ErrorConnect -> ErrorConnectFooter(viewModel)
            ConnectingStageType.Sync -> SyncFooter(glucometerName, state.connectingScreenConfig)
            ConnectingStageType.Complete -> CompleteFooter(viewModel, state.successfulSyncConfig)
            ConnectingStageType.ErrorSync -> ErrorSyncFooter(viewModel, state.failedSyncConfig)
        }
    }

    @Composable
    private fun CompleteFooter(
        viewModel: ConnectingViewModel,
        config: ScreenEntity?
    ) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(modifier = Modifier.padding(dimens.contentPadding)) {
                Row {
                    Text(
                        // Используем title из конфига или дефолтный
                        text = config?.title ?: stringResource(id = R.string.sync_state_title_sync_completed),
                        style = types.h1,
                    )
                    HSpacerSmall()
                    Image(
                        painter = painterResource(id = R.drawable.ic_connected),
                        contentDescription = null
                    )
                }
                VSpacerSmall()
                Text(
                    // Используем description из конфига или дефолтный
                    text = config?.description ?: stringResource(id = R.string.sync_state_subtitle_sync_completed),
                    color = colors.shadeBlack0
                )
                VSpacerSmall()
            }
            DownButton(
                widgetModel = viewModel.completeButton,
                onClickAction = ConnectingViewAction.ClickCompleteButton
            )
        }
    }



    @Composable
    private fun SyncFooter(
        glucometerName: String,
        config: ScreenEntity?
    ) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(Modifier.padding(dimens.contentPadding)) {
                Row {
                    Text(
                        text = config?.title ?: stringResource(id = R.string.sync_state_title_connected),
                        style = types.h1
                    )
                    HSpacerSmall()
                    Image(
                        painter = painterResource(id = R.drawable.ic_congrats),
                        contentDescription = null
                    )
                }
                VSpacerSmall()
                Text(
                    text = config?.description ?: stringResource(id = R.string.sync_connected_text, glucometerName),
                    color = colors.shadeBlack0
                )
                VSpacerSmall()
            }
            BaseSnackBar(textId = R.string.sync_process_text)
        }
    }

    @Composable
    private fun ErrorSyncFooter(
        viewModel: ConnectingViewModel,
        config: ScreenEntity?
    ) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(modifier = Modifier.padding(dimens.contentPadding)) {
                Text(
                    text = config?.title ?: stringResource(id = R.string.sync_connection_sync_error_title),
                    style = types.h1
                )
                VSpacerSmall()
                Text(
                    text = config?.description ?: stringResource(id = R.string.sync_connection_error_text),
                    color = colors.shadeBlack0
                )
                VSpacerSmall()
            }
            DownButton(
                widgetModel = viewModel.syncRepeatButton,
                onClickAction = ConnectingViewAction.ClickRepeatSyncButton
            )
        }
    }

    @Composable
    private fun ErrorConnectFooter(viewModel: ConnectingViewModel) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(modifier = Modifier.padding(dimens.contentPadding)) {
                Text(
                    text = stringResource(id = R.string.sync_connection_connect_error_title),
                    style = types.h1
                )
                VSpacerSmall()
                Text(
                    text = stringResource(id = R.string.sync_connection_error_text),
                    color = colors.shadeBlack0
                )
                VSpacerSmall()
            }
            DownButton(
                widgetModel = viewModel.connectRepeatButton,
                onClickAction = ConnectingViewAction.ClickRepeatButton
            )
        }
    }

    @Composable
    private fun DeviceNotFoundFooter(viewModel: ConnectingViewModel) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(modifier = Modifier.padding(dimens.contentPadding)) {
                Text(
                    text = stringResource(id = R.string.profile_device_search_not_found_title),
                    style = types.h1
                )
                VSpacerSmall()
                Text(
                    text = stringResource(id = R.string.profile_device_search_not_found_connect_title),
                    color = colors.shadeBlack0
                )
                VSpacer(height = dimens.halfMediumDim)
                TextNumericItem(
                    number = R.string.list_numbering_1_dot,
                    text = R.string.profile_device_search_not_found_disable_ble
                )
                VSpacerMedium()
                TextNumericItem(
                    number = R.string.list_numbering_2_dot,
                    text = R.string.profile_device_search_not_found_low_energy,
                )
                VSpacerMedium()
                TextNumericItem(
                    number = R.string.list_numbering_3_dot,
                    text = R.string.profile_device_search_not_found_out_of_range,
                )
                VSpacerSmall()
            }
            DownButton(
                widgetModel = viewModel.searchRepeatButton,
                onClickAction = ConnectingViewAction.ClickSearchButton
            )
        }
    }

    @Composable
    private fun ConnectingFooter(config: ScreenEntity?) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(Modifier.padding(dimens.contentPadding)) {
                Text(
                    text = config?.title ?: stringResource(id = R.string.sync_connection_screen_title),
                    style = types.h1
                )
                VSpacerSmall()
                Text(
                   text = config?.description ?: stringResource(id = R.string.sync_connection_screen_text),
                    color = colors.shadeBlack0
                )
            }
            VSpacerSmall()
            BaseSnackBar(textId = R.string.sync_device_finding_process_text)
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        val action = if (isGranted) ConnectingViewAction.Location.AllowPermission
        else ConnectingViewAction.Location.DeniedPermission

        viewModel sendAction action
    }

    private val locationEnableResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                viewModel sendAction ConnectingViewAction.Location.Enable
            }
            else -> { }
        }
    }

    private val bluetoothResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val connectAction = when (result.resultCode) {
            Activity.RESULT_OK -> ConnectingViewAction.Bluetooth.Enable
            else -> ConnectingViewAction.Bluetooth.Reject
        }
        viewModel sendAction connectAction
    }

}
