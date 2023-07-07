package com.elta.android.presentation.features.home.model

sealed class ManualSyncError {
    object NotFound : ManualSyncError()
    object ErrorSync : ManualSyncError()
}
