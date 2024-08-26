package com.elta.android.domain.features.consultant.model

import com.elta.android.domain.common.model.FileType

enum class ContentType {
    Text, Image, DocumentPdf, Voice, Video;

    companion object {
        fun ContentType.toFileType(): FileType {
            return when (this) {
                Image -> FileType.Jpg
                Voice -> FileType.Voice
                Video -> FileType.Mp4
                Text -> FileType.Text
                DocumentPdf -> FileType.Mp4
            }
        }

        fun FileType.toContentType(): ContentType {
            return when (this) {
                FileType.Text -> Text
                FileType.Pdf -> DocumentPdf
                FileType.Voice -> Voice
                FileType.Mp4 -> Video
                FileType.Png,
                FileType.Heif,
                FileType.Jpg -> Image
            }
        }
    }
}
