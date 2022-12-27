package com.elta.android.domain.features.consultant.model

data class ChatList(
    val messages: List<WebimMessage>,
    val hasNewMessage: Boolean
) {
    companion object {
        val emptyChat: ChatList =
            ChatList(
                messages = emptyList(),
                hasNewMessage = false
            )
    }
}
