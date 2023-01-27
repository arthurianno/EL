package com.elta.android.domain.features.consultant.model

sealed class WebimContentType(val extension: String) {
    object Text : WebimContentType(extension = "")
    object Jpg : WebimContentType(extension = ".jpg")
    object Png : WebimContentType(extension = ".png")
    object Heif : WebimContentType(extension = ".heif")
    object Pdf : WebimContentType(extension = ".pdf")
    object Voice : WebimContentType(extension = ".mp3")
}
