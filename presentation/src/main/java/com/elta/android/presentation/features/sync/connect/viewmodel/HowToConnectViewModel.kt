package com.elta.android.presentation.features.sync.connect.viewmodel

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

    private fun checkPermissions(permissionStates: List<PermissionState>) {
        if (permissionStates.all { it.status.isGranted }) {
            router.navigateTo(Screens.ScannerDmcScreen(state.value.isOnBoarding))
        } else {
            val cameraPermission = permissionStates.component1()
            val locationPermission = permissionStates.component2()
            when {
                !cameraPermission.status.isGranted && cameraPermission.status.shouldShowRationale -> cameraPermissionDialog.dialogOpen()
                !locationPermission.status.isGranted && locationPermission.status.shouldShowRationale -> locationPermissionDialog.dialogOpen()

                !cameraPermission.status.isGranted -> sendEvent(PermissionEvent.Camera())
                !locationPermission.status.isGranted -> sendEvent(PermissionEvent.FineLocation())
            }
        }
    }
}
