package com.elta.android.domain.features.consultant.model

data class WebimMessage(
    val id: String,
    val owner: WebimOwner,
    val text: String,
    val attachment: Attachment?,
    val time: Long,
    val sendStatus: WebimMessageSendStatus,
    val isRead: Boolean
) {
    data class Attachment(
        val contentType: WebimContentType?,
        val thumbnail: String?,
        val url: String?,
        val size: Long
    )
}
