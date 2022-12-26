package com.elta.android.domain.features.consultant.model

data class WebimMessage(
    val owner: WebimOwner,
    val type: WebimMessageType,
    val content: String
)
