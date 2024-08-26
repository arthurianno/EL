package com.elta.android.domain.common.model

sealed class FileType(val extension: String) {
    data object Jpg : FileType(extension = "jpg")
    data object Png : FileType(extension = "png")
    data object Heif : FileType(extension = "heif")
    data object Pdf : FileType(extension = "pdf")
    data object Voice : FileType(extension = "aac")
    data object Mp4 : FileType(extension = "mp4")
    data object Text : FileType(extension = "")

    companion object {
        fun getByExtension(extension: String): FileType =
            when (extension) {
                Jpg.extension -> Jpg
                Png.extension -> Png
                Heif.extension -> Heif
                Pdf.extension -> Pdf
                Voice.extension -> Voice
                Mp4.extension -> Mp4
                else -> Text
            }

        fun String?.toFileType(): FileType? =
            when (this) {
                MimeType.ImageJpg.mimeName -> Jpg
                MimeType.ImagePng.mimeName -> Png
                MimeType.ImageHeif.mimeName -> Heif
                MimeType.DocumentPdf.mimeName -> Pdf
                MimeType.Voice.mimeName -> Voice
                MimeType.Video.mimeName -> Mp4
                else -> null
            }
    }
}
