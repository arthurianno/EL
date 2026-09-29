package com.elta.android.presentation.features.sync.connect.varianta.model

import javax.annotation.concurrent.Immutable
// fixme Variant A : improved_enabling_location

@Immutable
data class ScannerDmcViewStateVariantA(
    val scannerState: ScannerStateVariantA,
    val isOnBoarding: Boolean
)
