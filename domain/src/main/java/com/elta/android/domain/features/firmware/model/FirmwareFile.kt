package com.elta.android.domain.features.firmware.model

data class FirmwareFile(
    val version: String,
    val compatible: String,
    val path: String,
    val hash: String
)
