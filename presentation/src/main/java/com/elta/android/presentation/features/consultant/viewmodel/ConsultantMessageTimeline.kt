package com.elta.android.presentation.features.consultant.viewmodel

import com.elta.android.domain.features.consultant.usecase.GetAudioDurationUseCase
import com.elta.android.domain.features.consultant.usecase.GetCachedFilesUriUseCase
import com.elta.android.presentation.features.consultant.model.AudioState
import com.elta.android.presentation.features.consultant.model.CachingState
import com.elta.android.presentation.features.consultant.model.MessageType
import com.elta.android.presentation.features.consultant.model.MessageType.Companion.toContentType
import com.elta.android.presentation.features.consultant.model.MessageUiEntity
import com.elta.android.presentation.features.consultant.viewmodel.analyzer.ChatMessagesAnalyzer
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConsultantMessageTimeline @Inject constructor(
    private val getCachedFilesUri: GetCachedFilesUriUseCase,
    private val getVoiceDuration: GetAudioDurationUseCase
) {
    suspend fun prepare(messages: List<MessageUiEntity>): List<MessageUiEntity> =
        withContext(Dispatchers.IO) { decorate(attachCachedFilesOnIo(messages)) }

    private fun attachCachedFilesOnIo(messages: List<MessageUiEntity>): List<MessageUiEntity> =
        messages.map { message ->
            val document = message.document
            if (document == null || message.type == MessageType.Text) return@map message

            val uri = getCachedFilesUri(
                fileName = document.fileName,
                type = message.type.toContentType()
            )
            val voiceDuration = if (message.type == MessageType.Voice) {
                getVoiceDuration(document.fileName)
            } else {
                null
            }

            message.copy(
                document = document.copy(
                    uri = uri,
                    cachingState = if (uri == null) CachingState.NotCached else CachingState.Cached
                ),
                audioState = voiceDuration?.let { duration ->
                    AudioState(isPlaying = false, duration = duration, trackPosition = 0)
                }
            )
        }

    private fun decorate(messages: List<MessageUiEntity>): List<MessageUiEntity> =
        ChatMessagesAnalyzer.defineChangingDateMessages(
            ChatMessagesAnalyzer.defineMessagesInSequence(messages)
        )
}
