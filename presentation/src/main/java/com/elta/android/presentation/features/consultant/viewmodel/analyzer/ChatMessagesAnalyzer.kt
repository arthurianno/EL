package com.elta.android.presentation.features.consultant.viewmodel.analyzer

import com.elta.android.presentation.features.consultant.model.CornerUiEntity
import com.elta.android.presentation.features.consultant.model.MessageType
import com.elta.android.presentation.features.consultant.model.MessageUiEntity
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId

object ChatMessagesAnalyzer {
    /**
     * Метод который определяет изменяется ли дата сообщения в списке и определяет есть ли другие сообщения с другой датой
     */
    fun defineChangingDateMessages(messages: List<MessageUiEntity>): List<MessageUiEntity> {
        if (messages.isEmpty()) return emptyList()

        var lastMessageDate: LocalDate? = null
        val dateSet = mutableSetOf<LocalDate>()

        messages.forEach { message ->
            val messageDate = Instant.ofEpochMilli(message.dateSending.timestamp)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            dateSet.add(messageDate)
        }

        val hasMultipleDates = dateSet.size > 1

        val updatedMessages = messages.mapIndexed { index, message ->
            val currentMessageDate = Instant.ofEpochMilli(message.dateSending.timestamp)
                .atZone(ZoneId.systemDefault()).toLocalDate()

            val isDayChanged = if (index == 0) {
                hasMultipleDates
            } else {
                val previousMessageDate =
                    Instant.ofEpochMilli(messages[index - 1].dateSending.timestamp)
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                currentMessageDate != previousMessageDate
            }

            message.copy(isDayChanged = isDayChanged).also {
                if (isDayChanged) {
                    lastMessageDate = currentMessageDate
                }
            }
        }

        return updatedMessages
    }

    /**
     * Определяет есть ли последовательность в текстовых сообщениях и ставить флаг где есть такое же сообщение
     * сверху или снизу.
     */
    fun defineMessagesInSequence(messages: List<MessageUiEntity>): List<MessageUiEntity> {
        val updatedMessages = messages.toMutableList()
        var currentChain: MutableList<MessageUiEntity> = mutableListOf()

        messages.forEachIndexed { index, message ->
            if (message.type == MessageType.Text) {
                if (currentChain.isEmpty()) {
                    currentChain.add(message)
                } else {
                    val lastMessage = currentChain.last()
                    if (lastMessage.owner == message.owner) {
                        currentChain.add(message)
                    } else {
                        processChain(currentChain, updatedMessages, messages)
                        currentChain = mutableListOf(message)
                    }
                }
            } else {
                processChain(currentChain, updatedMessages, messages)
                currentChain = mutableListOf()
            }

            if (index == messages.size - 1) {
                processChain(currentChain, updatedMessages, messages)
            }
        }

        return updatedMessages
    }

    private fun processChain(
        chain: List<MessageUiEntity>,
        updatedMessages: MutableList<MessageUiEntity>,
        originalMessages: List<MessageUiEntity>
    ) {
        for (i in chain.indices) {
            val prev = chain.getOrNull(i - 1)
            val next = chain.getOrNull(i + 1)
            val top = prev != null
            val bottom = next != null

            val updatedMessage = chain[i].copy(
                cornerSequence = CornerUiEntity(top = top, bottom = bottom)
            )
            val originalIndex = originalMessages.indexOf(chain[i])
            updatedMessages[originalIndex] = updatedMessage
        }
    }
}