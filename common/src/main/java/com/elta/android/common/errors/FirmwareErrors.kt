package com.elta.android.common.errors

object FirmwareDownloadingError : RuntimeException()
data class FirmwareNotSupportedByAppError(
    val version: String
) : RuntimeException("$version not supported by app.")