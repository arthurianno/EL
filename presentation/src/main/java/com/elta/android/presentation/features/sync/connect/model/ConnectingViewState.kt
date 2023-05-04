package com.elta.android.presentation.features.sync.connect.model

import com.elta.android.domain.features.devices.model.Glucometer

data class ConnectingViewState(
    val stageType: ConnectingStageType,
    val isOnBoarding: Boolean,
    val pinCode: String,
    val glucometerName: String,
    val connectDevice: Glucometer?
)
