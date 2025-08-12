package com.elta.android.presentation.features.newsChannel.ui.components.analyzer

import android.util.Log
import com.elta.android.presentation.features.consultant.model.CornerUiEntity
import com.elta.android.presentation.features.newsChannel.model.MessageUiEntity
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId

object NewsChatAnalyzer {
    fun defineChangingDateMessages(messages: List<MessageUiEntity>): List<MessageUiEntity> {
        if (messages.isEmpty()) return emptyList()

        var lastMessageDate: LocalDate? = null
        val dateSet = mutableSetOf<LocalDate>()

        messages.forEach { message ->
            val messageDate = Instant.ofEpochMilli(message.dateSending.timestamp)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            dateSet.add(messageDate)
            Log.e("NewsChatAnalyzer", "Message ${message.id}, date=$messageDate")
        }

        val hasMultipleDates = dateSet.size > 1
        Log.e("NewsChatAnalyzer", "Has multiple dates: $hasMultipleDates, dates=$dateSet")

        val updatedMessages = messages.mapIndexed { index, message ->
            val currentMessageDate = Instant.ofEpochMilli(message.dateSending.timestamp)
                .atZone(ZoneId.systemDefault()).toLocalDate()

            val isDayChanged = if (index == 0) {
                hasMultipleDates
            } else {
                val previousMessageDate = Instant.ofEpochMilli(messages[index - 1].dateSending.timestamp)
                    .atZone(ZoneId.systemDefault()).toLocalDate()
                currentMessageDate != previousMessageDate
            }

            Log.e("NewsChatAnalyzer", "Message ${message.id}, isDayChanged=$isDayChanged")
            message.copy(isDayChanged = isDayChanged).also {
                if (isDayChanged) {
                    lastMessageDate = currentMessageDate
                }
            }
        }

        return updatedMessages
    }

    fun defineMessagesInSequence(messages: List<MessageUiEntity>): List<MessageUiEntity> {
        val updatedMessages = messages.toMutableList()
        var currentChain: MutableList<MessageUiEntity> = mutableListOf()

        messages.forEachIndexed { index, message ->
            // Определяем, является ли сообщение текстовым
            val isTextMessage = message.text?.isNotEmpty() == true
            Log.e("NewsChatAnalyzer", "Processing message ${message.id}, isTextMessage=$isTextMessage")

            val prev = if (index > 0) messages[index - 1] else null
            val next = if (index < messages.size - 1) messages[index + 1] else null
            val top = prev != null
            val bottom = next != null

            if (isTextMessage) {
                currentChain.add(message)
            } else {
                Log.e("NewsChatAnalyzer", "Non-text message ${message.id}, processing chain: ${currentChain.map { it.id }}")
                processChain(currentChain, updatedMessages, messages)
                currentChain = mutableListOf()
                // Устанавливаем CornerUiEntity для сообщений Document и Image
                val updatedMessage = message.copy(cornerSequence = CornerUiEntity(top = top, bottom = bottom))
                updatedMessages[index] = updatedMessage
            }

            if (index == messages.size - 1) {
                Log.e("NewsChatAnalyzer", "End of list, processing final chain: ${currentChain.map { it.id }}")
                processChain(currentChain, updatedMessages, messages)
            }
        }
        Log.e("NewsChatAnalyzer", "Updated messages: ${updatedMessages.map { it.id to it.cornerSequence }}")
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
            // Используем индекс из оригинального списка
            val originalIndex = originalMessages.indexOfFirst { it === chain[i] }
            if (originalIndex != -1) {
                updatedMessages[originalIndex] = updatedMessage
            }
        }
    }
}