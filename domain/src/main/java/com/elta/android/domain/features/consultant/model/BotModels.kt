package com.elta.android.domain.features.consultant.model

import java.util.UUID

data class BotNode(
    val id: String,
    val text: String,
    val options: List<BotOption>
)

data class BotOption(
    val text: String,
    val nextNodeId: String,
    val analyticsEventName: String? = null
)

enum class MessageSender {
    BOT, USER
}

enum class MessageStatus {
    TYPING, DELIVERED
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val sender: MessageSender,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.DELIVERED
)
