package com.elta.android.presentation.features.devices.cgm

import com.elta.android.presentation.core.compose.common.Action

sealed class NmgMonitoringAction : Action {
    data object StopMonitoring : NmgMonitoringAction()
}
