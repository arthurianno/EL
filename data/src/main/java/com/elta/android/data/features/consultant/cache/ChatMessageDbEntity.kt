package com.elta.android.data.features.consultant.cache

import com.elta.android.domain.features.consultant.model.ChatMessage
import com.elta.android.domain.features.consultant.model.MessageSender
import com.elta.android.domain.features.consultant.model.MessageStatus
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class ChatMessageDbEntity(
    @Id var objectBoxId: Long = 0,
    val id: String,
    val text: String,
    val sender: String, // "BOT" / "USER"
    val timestamp: Long,
    val status: String // "TYPING" / "DELIVERED"
) {
    fun toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            text = text,
            sender = if (sender == "BOT") MessageSender.BOT else MessageSender.USER,
            timestamp = timestamp,
            status = if (status == "TYPING") MessageStatus.TYPING else MessageStatus.DELIVERED
        )
    }

    companion object {
        fun fromDomain(domain: ChatMessage): ChatMessageDbEntity {
            return ChatMessageDbEntity(
                id = domain.id,
                text = domain.text,
                sender = domain.sender.name,
                timestamp = domain.timestamp,
                status = domain.status.name
            )
        }
    }
}
