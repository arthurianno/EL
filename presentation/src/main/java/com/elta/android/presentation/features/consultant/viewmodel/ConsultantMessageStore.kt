package com.elta.android.presentation.features.consultant.viewmodel

import com.elta.android.domain.features.consultant.usecase.LoadLastMessagesUseCase
import com.elta.android.domain.features.consultant.usecase.LoadNextCachedMessagesUseCase
import com.elta.android.presentation.features.consultant.model.CachingState
import com.elta.android.presentation.features.consultant.model.MessageType
import com.elta.android.presentation.features.consultant.model.MessageUiEntity
import com.elta.android.presentation.features.consultant.model.toUi
import javax.inject.Inject
import kotlinx.coroutines.sync.Mutex

class ConsultantMessageStore @Inject constructor(
    private val loadLastMessages: LoadLastMessagesUseCase,
    private val loadNextMessages: LoadNextCachedMessagesUseCase,
    private val timeline: ConsultantMessageTimeline
) {
    private val mutex = Mutex()
    private var history: List<MessageUiEntity> = emptyList()
    private var live: List<MessageUiEntity> = emptyList()

    suspend fun updateLive(messages: List<MessageUiEntity>): List<MessageUiEntity> =
        updateAndPrepare { live = messages }

    suspend fun loadInitial(size: Int): List<MessageUiEntity>? {
        val page = loadLastMessages(size).toUi()
        if (page.isEmpty()) return null
        return updateAndPrepare { history = mergeMessages(history, page) }
    }

    suspend fun loadNext(size: Int): List<MessageUiEntity>? {
        val page = loadNextMessages(size).toUi()
        if (page.isEmpty()) return null
        return updateAndPrepare { history = mergeMessages(page, history) }
    }

    private suspend fun updateAndPrepare(update: () -> Unit): List<MessageUiEntity> {
        mutex.lock()
        try {
            update()
            return timeline.prepare(mergeMessages(history, live))
        } finally {
            mutex.unlock()
        }
    }
}

internal fun mergeMessages(
    history: List<MessageUiEntity>,
    live: List<MessageUiEntity>
): List<MessageUiEntity> {
    val result = ArrayList<MessageUiEntity>(history.size + live.size)
    val positions = HashMap<String, Int>()
    for (message in history + live) {
        val id = message.id
        val existing = if (id.isBlank()) null else positions[id]
        if (existing == null) {
            if (id.isNotBlank()) positions[id] = result.size
            result += message
        } else {
            result[existing] = message
        }
    }
    return result
}

internal fun preserveMessageInteractionState(
    prepared: List<MessageUiEntity>,
    displayed: List<MessageUiEntity>
): List<MessageUiEntity> {
    val previousById = displayed.filter { it.id.isNotBlank() }.associateBy { it.id }
    return prepared.map { message ->
        val previous = previousById[message.id] ?: return@map message
        val sameFile = message.document != null &&
            previous.document?.fileName == message.document?.fileName &&
            previous.document?.url == message.document?.url
        val document = if (sameFile && previous.document?.cachingState == CachingState.Downloading &&
            message.document?.cachingState == CachingState.NotCached
        ) {
            message.document?.copy(
                cachingState = CachingState.Downloading,
                uri = previous.document.uri
            )
        } else message.document
        val audioState = if (message.type == MessageType.Voice && sameFile) {
            previous.audioState ?: message.audioState
        } else message.audioState
        message.copy(document = document, audioState = audioState)
    }
}
