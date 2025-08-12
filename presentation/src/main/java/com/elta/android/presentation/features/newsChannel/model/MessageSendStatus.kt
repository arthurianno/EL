package com.elta.android.presentation.features.newsChannel.model

sealed class MessageSendStatus {
    object Sending : MessageSendStatus()
    object Sent : MessageSendStatus()
    data class Error(val message: String) : MessageSendStatus()
}