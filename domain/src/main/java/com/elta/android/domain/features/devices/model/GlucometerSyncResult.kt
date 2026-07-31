package com.elta.android.domain.features.devices.model

data class GlucometerSyncResult(
    val count: Int,
    val hasInvalidTime: Boolean = false
)
