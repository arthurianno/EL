package com.elta.android.presentation.features.devices.cgm

import com.elta.android.presentation.core.compose.common.Action

sealed class NmgSearchAction : Action {
    data object StartScan : NmgSearchAction()
    data class SelectSensor(val sensorId: String) : NmgSearchAction()
    data class NotificationPermissionResult(val isGranted: Boolean) : NmgSearchAction()
}
