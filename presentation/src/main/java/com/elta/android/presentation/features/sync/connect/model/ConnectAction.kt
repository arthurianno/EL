package com.elta.android.presentation.features.sync.connect.model

import com.elta.android.presentation.core.compose.common.Action
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState

sealed class ConnectAction : Action {
    object NeedHelp : ConnectAction()
    object CloseHelp : ConnectAction()
    object ConnectByPin : ConnectAction()
    object ConnectByDmc : ConnectAction()
    data class OnDmcReceived(val pin: String, val name: String) : ConnectAction()
    object SkipNextStep : ConnectAction()

    object ScannerError : ConnectAction()

    // fixme Variant A : improved_enabling_location
    data class StartConnecting(val pin: String, val name: String) : ConnectAction()
    object RepeatConnect : ConnectAction()
    object RepeatSync : ConnectAction()
    object RepeatSearch : ConnectAction()
    object Complete : ConnectAction()
    @OptIn(ExperimentalPermissionsApi::class)
    data class CheckPermissionsState(
        val permissionsStatus: List<PermissionState>
    ) : ConnectAction()

    object OpenConnectingScreen: ConnectAction()
}
