package com.elta.android.presentation.features.sync.connect

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import com.elta.android.presentation.core.compose.widgets.buttons.DownButton
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.core.compose.widgets.snackbar.BaseSnackBar
import com.elta.android.presentation.core.ui.fragment.addOnBackPressedCallback
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.ConnectMainEvent
import com.elta.android.presentation.features.sync.connect.model.connecting.ConnectingStageType
import com.elta.android.presentation.features.sync.connect.viewmodel.ConnectingViewModelVariantA
import com.elta.android.presentation.features.sync.connect.widgets.AppTopBar
import com.elta.android.presentation.features.sync.connect.widgets.HelpBottomSheetVariantA
import com.elta.android.presentation.features.sync.connect.widgets.MainImage
import com.elta.android.presentation.features.sync.connect.widgets.TextNumericItem
import com.elta.android.presentation.features.sync.control.PermissionsControl
import com.elta.android.presentation.features.sync.control.enableLocation
import com.elta.android.presentation.theme.GetLocalProperties
import com.elta.android.presentation.utils.bundle

// fixme Variant A : improved_enabling_location

class ConnectingFragmentVariantA : BaseComposeFragment<ConnectingViewModelVariantA>() {
    companion object {
        fun newInstance(
            isOnBoarding: Boolean,
            pin: String,
            name: String
        ) = ConnectingFragmentVariantA().apply {
            arguments = bundle(
                IS_ON_BOARDING_ARGUMENT_NAME to isOnBoarding,
                PIN_ARGUMENT_NAME to pin,
                GLUCOMETER_NAME_ARGUMENT_NAME to name
            )
        }
    }

    override val viewModel: ConnectingViewModelVariantA by viewModels { viewModelFactory }

    override fun ConnectingViewModelVariantA.init() {
        connectByPinButton.setText(getString(R.string.sync_connect_by_pin_boton_text))
        connectRepeatButton.setText(getString(R.string.repeat_connect_button_text))
        syncRepeatButton.setText(getString(R.string.repeat_sync_button_text))
        searchRepeatButton.setText(getString(R.string.repeat_search_button_text))
        completeButton.setText(getString(R.string.sync_state_action_sync_completed))
        appTopBar.setStartIconAction(AppAction.BackPressure)
        appTopBar.setEndIconAction(ConnectAction.NeedHelp)
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback { viewModel.backClick() }
    }

    @Composable
    override fun Dialogs(viewModel: ConnectingViewModelVariantA) {
        BaseDialog(widgetModel = viewModel.exitDialogFromConnecting)
        BaseDialog(widgetModel = viewModel.exitDialogFromSync)
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content(viewModel: ConnectingViewModelVariantA) {
        val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        val state = viewModel.state.collectAsState().value
        val event = viewModel.event.collectAsState(initial = null).value
        if (state.requestBluetoothActivation) { requestEnableBluetooth() }
        LaunchedEffect(key1 = event) {
            when (event) {
                is ConnectMainEvent.ShowSheet -> sheetState.show()
                is ConnectMainEvent.HideSheet -> sheetState.hide()
                is PermissionEvent.RequestPermissions -> requestLocationPermission()
                is PermissionEvent.RequestEnableLocation -> requestEnableLocation()
                else -> Unit
            }
        }
        GetLocalProperties { _, _, _, shapes, _ ->
            ModalBottomSheetLayout(
                sheetState = sheetState,
                sheetContent = {
                    HelpBottomSheetVariantA(
                        downButtonModel = viewModel.connectByPinButton,
                        closeOnClick = {
                            viewModel sendAction ConnectAction.CloseHelp
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
        viewModel: ConnectingViewModelVariantA,
        stageType: ConnectingStageType
    ) {
        val isCompleted =
            stageType == ConnectingStageType.Complete || stageType == ConnectingStageType.Sync
        AppTopBar(
            appTopBarModel = viewModel.appTopBar,
            startIcon = R.drawable.ic_back.takeUnless { stageType == ConnectingStageType.Complete },
            endText = R.string.sync_connect_type_button_any_difficulties.takeUnless { isCompleted }
        )
        MainImage(
            imageId = if (isCompleted) {
                R.drawable.ic_connect_finish
            } else {
                R.drawable.ic_connect_dev
            }
        )
    }

    @Composable
    private fun Footer(
        viewModel: ConnectingViewModelVariantA,
        stageType: ConnectingStageType,
        glucometerName: String
    ) {
        when (stageType) {
            ConnectingStageType.Connecting -> ConnectingFooter()
            ConnectingStageType.DeviceNotFound -> DeviceNotFoundFooter(viewModel)
            ConnectingStageType.ErrorConnect -> ErrorConnectFooter(viewModel)
            ConnectingStageType.Sync -> SyncFooter(glucometerName)
            ConnectingStageType.Complete -> CompleteFooter(viewModel)
            ConnectingStageType.ErrorSync -> ErrorSyncFooter(viewModel)
        }
    }

    @Composable
    private fun CompleteFooter(viewModel: ConnectingViewModelVariantA) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(modifier = Modifier.padding(dimens.contentPadding)) {
                Row {
                    Text(
                        text = stringResource(id = R.string.sync_state_title_sync_completed),
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
                    text = stringResource(id = R.string.sync_state_subtitle_sync_completed),
                    style = types.body1,
                    color = colors.shadeBlack0
                )
                VSpacerSmall()
            }
            DownButton(
                widgetModel = viewModel.completeButton,
                onClickAction = ConnectAction.Complete
            )
        }
    }

    @Composable
    private fun SyncFooter(glucometerName: String) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(Modifier.padding(dimens.contentPadding)) {
                Row {
                    Text(
                        text = stringResource(id = R.string.sync_state_title_connected),
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
                    text = stringResource(id = R.string.sync_connected_text, glucometerName),
                    style = types.body1,
                    color = colors.shadeBlack0
                )
                VSpacerSmall()
            }
            BaseSnackBar(textId = R.string.sync_process_text)
        }
    }

    @Composable
    private fun ErrorSyncFooter(viewModel: ConnectingViewModelVariantA) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(modifier = Modifier.padding(dimens.contentPadding)) {
                Text(
                    text = stringResource(id = R.string.sync_connection_sync_error_title),
                    style = types.h1
                )
                VSpacerSmall()
                Text(
                    text = stringResource(id = R.string.sync_connection_error_text),
                    style = types.body1,
                    color = colors.shadeBlack0
                )
                VSpacerSmall()
            }
            DownButton(
                widgetModel = viewModel.syncRepeatButton,
                onClickAction = ConnectAction.RepeatSync
            )
        }
    }

    @Composable
    private fun ErrorConnectFooter(viewModel: ConnectingViewModelVariantA) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(modifier = Modifier.padding(dimens.contentPadding)) {
                Text(
                    text = stringResource(id = R.string.sync_connection_connect_error_title),
                    style = types.h1
                )
                VSpacerSmall()
                Text(
                    text = stringResource(id = R.string.sync_connection_error_text),
                    style = types.body1,
                    color = colors.shadeBlack0
                )
                VSpacerSmall()
            }
            DownButton(
                widgetModel = viewModel.connectRepeatButton,
                onClickAction = ConnectAction.RepeatConnect
            )
        }
    }

    @Composable
    private fun DeviceNotFoundFooter(viewModel: ConnectingViewModelVariantA) {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(modifier = Modifier.padding(dimens.contentPadding)) {
                Text(
                    text = stringResource(id = R.string.profile_device_search_not_found_title),
                    style = types.h1
                )
                VSpacerSmall()
                Text(
                    text = stringResource(id = R.string.profile_device_search_not_found_connect_title),
                    style = types.body1,
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
                onClickAction = ConnectAction.RepeatSearch
            )
        }
    }

    @Composable
    private fun ConnectingFooter() {
        GetLocalProperties { dimens, _, colors, _, types ->
            Column(Modifier.padding(dimens.contentPadding)) {
                Text(
                    text = stringResource(id = R.string.sync_connection_screen_title),
                    style = types.h1
                )
                VSpacerSmall()
                Text(
                    text = stringResource(id = R.string.sync_connection_screen_text),
                    style = types.body1,
                    color = colors.shadeBlack0
                )
            }
            VSpacerSmall()
            BaseSnackBar(textId = R.string.sync_device_finding_process_text)
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
            Activity.RESULT_OK -> ConnectAction.RepeatSearch
            else -> ConnectAction.ScannerError
        }
        viewModel sendAction connectAction
    }

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (isRequiredPermissionsGranted()) requestEnableLocation()
        else viewModel sendAction ConnectAction.ScannerError
    }

    private fun requestLocationPermission() {
        val permissions = buildList {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                !isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!isPermissionGranted(Manifest.permission.BLUETOOTH_SCAN)) {
                    add(Manifest.permission.BLUETOOTH_SCAN)
                }
                if (!isPermissionGranted(Manifest.permission.BLUETOOTH_CONNECT)) {
                    add(Manifest.permission.BLUETOOTH_CONNECT)
                }
            }
        }

        if (permissions.isEmpty()) {
            requestEnableLocation()
        } else {
            permissionsLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun isRequiredPermissionsGranted(): Boolean =
        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
                isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                        (
                                isPermissionGranted(Manifest.permission.BLUETOOTH_SCAN) &&
                                        isPermissionGranted(Manifest.permission.BLUETOOTH_CONNECT)
                                ))

    private fun isPermissionGranted(permission: String): Boolean =
        requireContext().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun requestEnableLocation() {
        enableLocation(this) {
            viewModel sendAction ConnectAction.RepeatSearch
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PermissionsControl.REQUEST_CODE_ENABLE_LOCATION) {
            val action = if (resultCode == Activity.RESULT_OK) {
                ConnectAction.RepeatSearch
            } else {
                ConnectAction.ScannerError
            }
            viewModel sendAction action
        }
    }

}
