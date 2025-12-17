package com.elta.android.presentation.features.sync.connect.model

import com.elta.android.domain.features.multiLangsConfig.model.ScreenEntity
import javax.annotation.concurrent.Immutable

@Immutable
data class ConnectStartViewState(
    val isOnBoarding: Boolean,
    val screenConfig: ScreenEntity? = null,
    val isContentReady: Boolean = false
)
