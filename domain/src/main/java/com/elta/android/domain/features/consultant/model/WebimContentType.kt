package com.elta.android.domain.features.consultant.model

sealed class WebimContentType(val extension: String) {

    companion object {
        fun getByExtension(extension: String): WebimContentType =
            when (extension) {
                Jpg.extension -> Jpg
                Png.extension -> Png
                Heif.extension -> Heif
                Pdf.extension -> Pdf
                Voice.extension -> Voice
                else -> Text
            }
    }

    object Text : WebimContentType(extension = "")
    object Jpg : WebimContentType(extension = "jpg")
    object Png : WebimContentType(extension = "png")
    object Heif : WebimContentType(extension = "heif")
    object Pdf : WebimContentType(extension = "pdf")
    object Voice : WebimContentType(extension = "aac")
}
