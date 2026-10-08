package com.elta.android.presentation.features.consultant.viewmodel

import com.elta.android.domain.features.consultant.model.MessageOwner
import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import com.elta.android.presentation.features.consultant.model.AudioState
import com.elta.android.presentation.features.consultant.model.CachingState
import com.elta.android.presentation.features.consultant.model.DateUiEntity
import com.elta.android.presentation.features.consultant.model.DocumentUiEntity
import com.elta.android.presentation.features.consultant.model.MessageType
import com.elta.android.presentation.features.consultant.model.MessageUiEntity
import com.elta.android.presentation.features.consultant.viewmodel.analyzer.ChatMessagesAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Test

class ConsultantMessageStoreTest {
    @Test
    fun `history keeps its order and live version replaces a matching message`() {
        val older = message("older")
        val overlap = message("same", text = "cached")
        val latest = message("latest")

        val merged = mergeMessages(listOf(older, overlap), listOf(overlap.copy(text = "live"), latest))

        assertEquals(listOf("older", "same", "latest"), merged.map { it.id })
        assertEquals("live", merged[1].text)
    }

    @Test
    fun `messages without server ids remain distinct`() {
        val pending = message("")

        assertEquals(2, mergeMessages(listOf(pending), listOf(pending)).size)
    }

    @Test
    fun `timeline refresh keeps an active download and voice playback`() {
        val voice = message("voice", type = MessageType.Voice).copy(
            document = DocumentUiEntity(
                fileName = "voice.mp3",
                fileType = MessageType.Voice,
                url = "https://example.test/voice.mp3",
                size = null,
                isPortrait = null,
                cachingState = CachingState.NotCached,
                uri = null
            ),
            audioState = AudioState(isPlaying = false, duration = 10, trackPosition = 0)
        )
        val displayed = voice.copy(
            document = voice.document?.copy(cachingState = CachingState.Downloading),
            audioState = AudioState(isPlaying = true, duration = 10, trackPosition = 4)
        )

        val preserved = preserveMessageInteractionState(listOf(voice), listOf(displayed)).single()

        assertEquals(CachingState.Downloading, preserved.document?.cachingState)
        assertEquals(displayed.audioState, preserved.audioState)
    }

    @Test
    fun `sequence markers use positions for identical messages`() {
        val repeated = message("", text = "same")

        val result = ChatMessagesAnalyzer.defineMessagesInSequence(listOf(repeated, repeated))

        assertEquals(false, result[0].cornerSequence?.top)
        assertEquals(true, result[0].cornerSequence?.bottom)
        assertEquals(true, result[1].cornerSequence?.top)
        assertEquals(false, result[1].cornerSequence?.bottom)
    }

    private fun message(
        id: String,
        text: String = "text",
        type: MessageType = MessageType.Text
    ) = MessageUiEntity(
        id = id,
        text = text,
        document = null,
        owner = MessageOwner.User,
        sendingStatus = WebimMessageSendStatus.Sent,
        timeSending = "12:00",
        dateSending = DateUiEntity.Today(1_700_000_000_000),
        type = type,
        isRead = true,
        isEdited = false,
        canBeEdit = true,
        isDayChanged = false,
        cornerSequence = null,
        audioState = null
    )
}
