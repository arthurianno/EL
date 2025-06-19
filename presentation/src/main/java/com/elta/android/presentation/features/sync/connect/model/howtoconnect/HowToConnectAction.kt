package com.elta.android.presentation.features.sync.connect.model.howtoconnect

import com.elta.android.presentation.core.compose.common.Action

sealed interface HowToConnectAction : Action {

    data object OnConnectButtonClick : HowToConnectAction

    object Camera {
        data object AppearPermission : HowToConnectAction
        data class AllowPermission(val isAlreadyGranted: Boolean) : HowToConnectAction
        data object ShowPermissionRationale : HowToConnectAction
    }

    object Bluetooth {
        data object AppearPermission : HowToConnectAction
        data class AllowPermission(val isAlreadyGranted: Boolean) : HowToConnectAction
        data object ShowPermissionRationale : HowToConnectAction
        data object Enabled : HowToConnectAction
        data object Rejected : HowToConnectAction
    }

    object Location {
        data object AppearPermission : HowToConnectAction
        data class AllowPermission(val isAlreadyGranted: Boolean) : HowToConnectAction
        data object ShowPermissionRationale : HowToConnectAction
        data object Enabled : HowToConnectAction
    }
}
