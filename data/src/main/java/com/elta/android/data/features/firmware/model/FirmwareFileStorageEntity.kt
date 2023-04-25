package com.elta.android.data.features.firmware.model

data class FirmwareFileStorageEntity(
    val version: String,
    val compatible: String,
    val path: String,
    val hash: String
)
