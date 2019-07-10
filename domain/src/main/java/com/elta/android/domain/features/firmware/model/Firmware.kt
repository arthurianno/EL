package com.elta.android.domain.features.firmware.model

data class Firmware(
    val version: String,
    val compatible: String,
    val hash: String,
    val isCompatibleWithApplication: Boolean = false
)