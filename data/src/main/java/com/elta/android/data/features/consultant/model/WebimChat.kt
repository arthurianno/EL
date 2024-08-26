package com.elta.android.data.features.consultant.model

import ru.webim.android.sdk.Message

data class WebimChat(
    val id: String,
    val messages: List<Message>,
    val hasNewMessage: Boolean
) {
    companion object {
        val emptyChat: WebimChat =
            WebimChat(
                id = "-1",
                messages = emptyList(),
                hasNewMessage = false
            )
    }
}
