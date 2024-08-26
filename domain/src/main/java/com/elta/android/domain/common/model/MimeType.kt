package com.elta.android.domain.common.model


enum class MimeType(val mimeName: String) {
    Image("image/*"),
    ImageJpg("image/jpeg"),
    ImagePng("image/png"),
    ImageHeif("image/heif"),

    DocumentPdf("application/pdf"),

    Video("video/mp4"),

    Voice("audio/aac")
}
