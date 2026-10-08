package com.elta.android.presentation.features.consultant.viewmodel

import android.net.Uri
import com.elta.android.presentation.features.consultant.model.MessageUiEntity
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

/** Keeps the active voice message and playback callbacks in one place. */
class ConsultantPlaybackController @Inject constructor(
    private val player: ConsultantAudioPlayer
) {
    private var activeMessageId: String? = null

    fun play(
        scope: CoroutineScope,
        message: MessageUiEntity,
        onStateChanged: (messageId: String, isPlaying: Boolean, position: Int?) -> Unit
    ) {
        val uri = message.document?.uri ?: return
        val position = message.audioState?.trackPosition?.takeIf { it != 0 }
        start(scope, message.id, uri, position, onStateChanged)
    }

    fun seek(
        scope: CoroutineScope,
        message: MessageUiEntity,
        fraction: Float,
        onStateChanged: (messageId: String, isPlaying: Boolean, position: Int?) -> Unit
    ) {
        val uri = message.document?.uri ?: return
        val duration = message.audioState?.duration ?: return
        val position = (duration * fraction.coerceIn(0f, 1f)).toInt()
        start(scope, message.id, uri, position, onStateChanged)
    }

    fun pause(
        messageId: String,
        onStateChanged: (messageId: String, isPlaying: Boolean, position: Int?) -> Unit
    ) {
        if (activeMessageId == messageId) pauseActive(onStateChanged)
    }

    fun pauseActive(
        onStateChanged: (messageId: String, isPlaying: Boolean, position: Int?) -> Unit
    ) {
        val messageId = activeMessageId ?: return
        activeMessageId = null
        val position = player.pause()
        onStateChanged(messageId, false, position)
    }

    private fun start(
        scope: CoroutineScope,
        messageId: String,
        uri: Uri,
        position: Int?,
        onStateChanged: (messageId: String, isPlaying: Boolean, position: Int?) -> Unit
    ) {
        pauseActive(onStateChanged)
        activeMessageId = messageId
        onStateChanged(messageId, true, position)
        player.play(
            scope = scope,
            uri = uri,
            position = position,
            onPosition = { current ->
                if (activeMessageId == messageId) onStateChanged(messageId, true, current)
            },
            onFinished = {
                if (activeMessageId == messageId) {
                    activeMessageId = null
                    onStateChanged(messageId, false, 0)
                }
            }
        )
    }
}
