package com.elta.android.presentation.features.sync.connect.model

import com.elta.android.domain.features.devices.model.Glucometer
import com.elta.android.presentation.features.sync.connect.model.connecting.ConnectingStageType
// fixme Variant A : improved_enabling_location

data class ConnectingViewStateVariantA(
    val stageType: ConnectingStageType,
    val isOnBoarding: Boolean,
    val pinCode: String,
    val glucometerName: String,
    val connectDevice: Glucometer?,
    val requestBluetoothActivation: Boolean,
)
