package com.elta.android.common.errors

object FirmwareDownloadingError : RuntimeException()

class FirmwareUpdateError(
    message: String
) : RuntimeException(message)

object NoSuchFirmware : RuntimeException()
