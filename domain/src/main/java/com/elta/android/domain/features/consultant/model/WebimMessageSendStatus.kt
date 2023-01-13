package com.elta.android.domain.features.consultant.model

sealed class WebimMessageSendStatus {
    object Sending : WebimMessageSendStatus()
    object Sent : WebimMessageSendStatus()
    data class Error(val message: String) : WebimMessageSendStatus()
}
