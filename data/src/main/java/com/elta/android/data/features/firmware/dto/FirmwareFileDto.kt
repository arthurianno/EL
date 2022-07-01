package com.elta.android.data.features.firmware.dto

data class FirmwareFileDto(
    val version: String,
    val compatible: String,
    val path: String,
    val hash: String
)
