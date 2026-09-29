package com.elta.android.presentation.features.sync.connect.model

import javax.annotation.concurrent.Immutable

@Immutable
data class ScannerDmcViewState(
    val scannerState: ScannerState,
    val isOnBoarding: Boolean
)
