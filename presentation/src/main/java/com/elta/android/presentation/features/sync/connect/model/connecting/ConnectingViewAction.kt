package com.elta.android.presentation.features.sync.connect.model.connecting

import com.elta.android.presentation.core.compose.common.Action

sealed interface ConnectingViewAction : Action {
    object Location {
        data object AllowPermission : ConnectingViewAction
        data object DeniedPermission : ConnectingViewAction
        data object ShowPermissionRationale : ConnectingViewAction
        data object Enable : ConnectingViewAction
    }

    object Bluetooth {
        data object Enable : ConnectingViewAction
        data object Reject : ConnectingViewAction
    }

    data object CloseHelp : ConnectingViewAction
    data object OpenHelp : ConnectingViewAction
    data object ClickCompleteButton : ConnectingViewAction
    data object ClickRepeatSyncButton : ConnectingViewAction
    data object ClickRepeatButton : ConnectingViewAction
    data object ClickSearchButton : ConnectingViewAction
    data object OnConnectClick : ConnectingViewAction

}
