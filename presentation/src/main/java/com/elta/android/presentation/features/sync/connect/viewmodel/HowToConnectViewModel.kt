package com.elta.android.presentation.features.sync.connect.viewmodel

import android.os.Build
import android.os.Bundle
import androidx.camera.lifecycle.ExperimentalCameraProviderConfiguration
import com.elta.android.presentation.Screens
import com.elta.android.presentation.analytic.core.appmetric.AppMetricTracker
import com.elta.android.presentation.analytic.getMetricName
import com.elta.android.presentation.analytic.model.appmetric.AppMetricEvent
import com.elta.android.presentation.analytic.model.appmetric.models.AlertType
import com.elta.android.presentation.analytic.model.appmetric.params.TurningResultParam
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
class HowToConnectViewModel @Inject constructor(
    private val appMetric: AppMetricTracker
) : BaseViewModel<HowToConnectViewState>() {
    override fun createInitState(): HowToConnectViewState =
        HowToConnectViewState(
            isOnBoarding = false
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
            is ConnectAction.CheckPermissionsState -> checkPermissions(action.permissionsStatus)
            is ConnectAction.OpenConnectingScreen ->
                router.navigateTo(Screens.ScannerDmcScreen(state.value.isOnBoarding))
        }
    }

    override fun reduceStateByAction(
        currentState: HowToConnectViewState,
        action: Action
    ): HowToConnectViewState = run {
        when (action) {
            is ConnectAction.Complete -> {
                appMetric.trackEvent(AppMetricEvent.BluetoothTurningAlertClick(TurningResultParam.ALLOW))
                sendEvent(PermissionEvent.Bluetooth.OnAllow)
                currentState
            }

            is ConnectAction.RepeatConnect -> {
                appMetric.trackEvent(AppMetricEvent.BluetoothTurningAlertClick(TurningResultParam.REJECT))
                currentState
            }

            else -> currentState
        }
    }

    private fun checkPermissions(permissionStates: List<PermissionState>) {
        val cameraPermission = permissionStates.component1()
        val locationPermission = permissionStates.component2()
            .takeIf { isLocationPermissionNeeded() }

        val bluetoothPermission =
            listOf(permissionStates.component3(), permissionStates.component4())
                .takeIf { isBlePermissionsNeeded() }

        val commonPermissions = listOf(cameraPermission)
        val permissions = bluetoothPermission?.let {
            it.toMutableList() + commonPermissions
        } ?: commonPermissions

        appMetric.trackEvent(cameraPermission.getMetricName(AlertType.Camera))
        bluetoothPermission?.first()?.getMetricName(AlertType.Camera)
            ?.let { appMetric.trackEvent(it) }
        locationPermission?.getMetricName(AlertType.Camera)
            ?.let { appMetric.trackEvent(it) }

        when {
            cameraPermission.status.shouldShowRationale ->
                cameraPermissionDialog.dialogOpen()

            locationPermission?.status?.shouldShowRationale ?: false ->
                locationPermissionDialog.dialogOpen()

            bluetoothPermission?.any { it.status.shouldShowRationale } ?: false ->
                bluetoothPermissionDialog.dialogOpen()

            permissions.all { it.status.isGranted } -> checkLocationAndBluetoothState()

            else -> sendEvent(PermissionEvent.RequestPermissions)
        }
    }

    private fun checkLocationAndBluetoothState() {
        val event =
            if (isLocationPermissionNeeded()) PermissionEvent.RequestEnableLocation
            else {
                appMetric.trackEvent(AppMetricEvent.BluetoothTurningAlert)
                PermissionEvent.Bluetooth.RequestEnable
            }

        sendEvent(event)
    }

    private fun isLocationPermissionNeeded(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    private fun isBlePermissionsNeeded(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

}
