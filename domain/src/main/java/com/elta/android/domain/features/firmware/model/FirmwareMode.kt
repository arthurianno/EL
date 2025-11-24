package com.elta.android.domain.features.firmware.model

enum class FirmwareMode {
    NordicDfu, Boot;

    companion object {
        fun toFirmwareMode(value: String): FirmwareMode =
            when (value) {
                BOOT_MODE_NAME -> Boot
                else -> NordicDfu
            }
    }
}

private const val BOOT_MODE_NAME = "boot-mode"
