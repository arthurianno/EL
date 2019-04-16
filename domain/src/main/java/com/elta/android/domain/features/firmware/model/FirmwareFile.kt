package com.elta.android.domain.features.firmware.model

data class FirmwareFile(
    val compatible: String,
    val path: String,
    val hash: String
)