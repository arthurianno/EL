package com.elta.android.domain.common.model

sealed class FileType(val extension: String) {
    object Jpg : FileType(extension = "jpg")
    object Png : FileType(extension = "png")
    object Heif : FileType(extension = "heif")
    object Pdf : FileType(extension = "pdf")
    object Voice : FileType(extension = "m4a")
}
