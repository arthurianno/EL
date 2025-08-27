package com.elta.android.presentation.features.sync.connect.model

import com.elta.android.domain.features.multiLang.entities.ScreenConfig
import javax.annotation.concurrent.Immutable

@Immutable
data class ConnectStartViewState(
    val isOnBoarding: Boolean,
    val screenConfig: ScreenConfig?
)
