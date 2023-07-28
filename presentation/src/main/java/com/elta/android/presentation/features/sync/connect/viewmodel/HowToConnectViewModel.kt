package com.elta.android.presentation.features.sync.connect.viewmodel

import android.os.Build
import android.os.Bundle
import androidx.camera.lifecycle.ExperimentalCameraProviderConfiguration
import com.elta.android.presentation.Screens
import com.elta.android.presentation.core.compose.common.Action
import com.elta.android.presentation.core.compose.common.AppAction
import com.elta.android.presentation.core.compose.common.PermissionEvent
import com.elta.android.presentation.core.compose.viewmodel.BaseViewModel
import com.elta.android.presentation.core.compose.widgets.appbar.BaseAppTopBarWidgetModel
import com.elta.android.presentation.core.compose.widgets.buttons.DownButtonWidgetModel
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.features.sync.connect.IS_ON_BOARDING_ARGUMENT_NAME
import com.elta.android.presentation.features.sync.connect.model.ConnectAction
import com.elta.android.presentation.features.sync.connect.model.HowToConnectViewState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import javax.inject.Inject

@ExperimentalCameraProviderConfiguration
@OptIn(ExperimentalPermissionsApi::class)
class HowToConnectViewModel @Inject constructor() : BaseViewModel<HowToConnectViewState>() {
    override fun createInitState(): HowToConnectViewState =
        HowToConnectViewState(
            isOnBoarding = false,
            bluetoothEnabled = false
        )

    val appTopBar = BaseAppTopBarWidgetModel()
    val downButton = DownButtonWidgetModel()

    val cameraPermissionDialog = BaseDialogWidgetModel<Nothing>(
        positiveOnCLick = { sendEvent(PermissionEvent.OpenSettings) }
    )

    val locationPermissionDialog = BaseDialogWidgetModel<Nothing>(
        positiveOnCLick = { sendEvent(PermissionEvent.OpenSettings) }
    )

    val bluetoothPermissionDialog = BaseDialogWidgetModel<Nothing>(
        positiveOnCLick = { sendEvent(PermissionEvent.OpenSettings) }
    )

    override val widgets = listOf(
        appTopBar,
        downButton
    ).actionObserve()

    override fun handleFragmentArguments(arguments: Bundle) {
        reduceState {
            state.value.copy(
                isOnBoarding = arguments.getBoolean(
                    IS_ON_BOARDING_ARGUMENT_NAME
                )
            )
        }
    }

    override fun handleUserAction(action: Action) {
        when (action) {
            is AppAction.BackPressure -> backClick()
            is ConnectAction.OpenConnectingScreen -> checkPermissions(action.permissionsStatus)
        }
    }

    override fun reduceStateByAction(
        currentState: HowToConnectViewState,
        action: Action
    ): HowToConnectViewState = run {
        when (action) {
            is ConnectAction.Complete -> {
                state.value.copy(bluetoothEnabled = true)
            }
            is ConnectAction.RepeatConnect -> {
                state.value.copy(bluetoothEnabled = false)
            }
            else -> currentState
        }
    }

    private fun checkPermissions(permissionStates: List<PermissionState>) {
        val cameraPermission = permissionStates.component1()
        val locationPermission = permissionStates.component2()
        val bluetoothPermission = buildBluetoothPermission(permissionStates)
        val permissions = listOf(cameraPermission, locationPermission, bluetoothPermission)

        if (permissions.all { it.status.isGranted } && state.value.bluetoothEnabled) {
            router.navigateTo(Screens.ScannerDmcScreen(state.value.isOnBoarding))
        } else {
            if (bluetoothPermission.status.isGranted) {
                reduceState { state.value.copy(bluetoothEnabled = true) }
            }
            when {
                !cameraPermission.status.isGranted && cameraPermission.status.shouldShowRationale -> cameraPermissionDialog.dialogOpen()
                !locationPermission.status.isGranted && locationPermission.status.shouldShowRationale -> locationPermissionDialog.dialogOpen()
                !bluetoothPermission.status.isGranted -> bluetoothPermissionDialog.dialogOpen()

                !cameraPermission.status.isGranted -> sendEvent(PermissionEvent.Camera())
                !locationPermission.status.isGranted -> sendEvent(PermissionEvent.FineLocation())
            }
        }
    }

    private fun buildBluetoothPermission(permissionStates: List<PermissionState>) =
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            permissionStates.component4()
        } else permissionStates.component3()
}
