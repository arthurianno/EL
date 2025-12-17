package com.elta.android.presentation.features.sync.connect.model.howtoconnect

import androidx.compose.runtime.Immutable
import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity

@Immutable
data class HowToConnectViewState(
    val isOnBoarding: Boolean,
    val screenConfig: ScreenEntity? = null,
    val isContentReady: Boolean = false
)
