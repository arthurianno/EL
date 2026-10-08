package com.elta.android.presentation.features.consultant.viewmodel.analyzer

import com.elta.android.presentation.features.consultant.model.CornerUiEntity
import com.elta.android.presentation.features.consultant.model.MessageType
import com.elta.android.presentation.features.consultant.model.MessageUiEntity
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId

object ChatMessagesAnalyzer {
    fun defineChangingDateMessages(messages: List<MessageUiEntity>): List<MessageUiEntity> {
        if (messages.isEmpty()) return emptyList()

        val zone = ZoneId.systemDefault()
        val dates = messages.map { message ->
            Instant.ofEpochMilli(message.dateSending.timestamp).atZone(zone).toLocalDate()
        }
        val hasMultipleDates = dates.any { it != dates.first() }

        return messages.mapIndexed { index, message ->
            val isDayChanged = if (index == 0) hasMultipleDates else dates[index] != dates[index - 1]
            message.copy(isDayChanged = isDayChanged)
        }
    }

    fun defineMessagesInSequence(messages: List<MessageUiEntity>): List<MessageUiEntity> =
        messages.mapIndexed { index, message ->
            if (message.type != MessageType.Text) return@mapIndexed message

            val previous = messages.getOrNull(index - 1)
            val next = messages.getOrNull(index + 1)
            message.copy(
                cornerSequence = CornerUiEntity(
                    top = previous?.type == MessageType.Text && previous.owner == message.owner,
                    bottom = next?.type == MessageType.Text && next.owner == message.owner
                )
            )
        }
}
