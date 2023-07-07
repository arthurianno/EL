package com.elta.android.domain.features.firmware.model

data class FirmwareInfo(
    val id: String,
    val version: String,
    val size: Int,
    val hash: String
)
