package com.elta.android.domain.features.consultant.model

data class ConsultantChat(
    val id: String,
    val messages: List<ConsultantMessage>,
    val hasNewMessage: Boolean
)
