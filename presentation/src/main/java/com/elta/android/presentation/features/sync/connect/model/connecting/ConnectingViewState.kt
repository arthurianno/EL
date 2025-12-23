package com.elta.android.presentation.features.sync.connect.model.connecting

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity

data class ConnectingViewState(
    val stageType: ConnectingStageType,
    val isOnBoarding: Boolean,
    val pinCode: String,
    val glucometerName: String,
    val connectDevice: Glucometer?,
    val connectingScreenConfig: ScreenEntity? = null,
    val successfulSyncConfig: ScreenEntity? = null,
    val failedSyncConfig: ScreenEntity? = null,
    val isConnectingImageReady : Boolean = false,
    val isSuccessImageReady : Boolean = false,
    val isFailedImageReady : Boolean = false

)
