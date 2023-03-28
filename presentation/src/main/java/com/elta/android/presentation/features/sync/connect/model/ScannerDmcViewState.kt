package com.elta.android.presentation.features.sync.connect.model

import androidx.compose.ui.unit.DpSize
import javax.annotation.concurrent.Immutable

@Immutable
data class ScannerDmcViewState(
    val scannerState: ScannerState,
    val cropRect: DpSize,
    val isOnBoarding: Boolean
)
