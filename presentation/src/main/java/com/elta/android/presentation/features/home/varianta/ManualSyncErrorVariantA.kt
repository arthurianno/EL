package com.elta.android.presentation.features.home.model

sealed class ManualSyncErrorVariantA {
    object NotFound : ManualSyncErrorVariantA()
    object ErrorSync : ManualSyncErrorVariantA()
}
