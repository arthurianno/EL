package com.elta.android.presentation.features.consultant.model

import com.elta.android.domain.features.consultant.model.ContentType

enum class MessageType {
    Text,
    Image,
    Document,
    Video,
    Voice;

    companion object {
        fun MessageType.toContentType(): ContentType =
            when(this){
                Text -> ContentType.Text
                Image -> ContentType.Image
                Document -> ContentType.DocumentPdf
                Video -> ContentType.Video
                Voice -> ContentType.Voice
            }
    }
}