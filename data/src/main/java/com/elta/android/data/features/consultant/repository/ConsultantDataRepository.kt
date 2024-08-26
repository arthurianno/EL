package com.elta.android.data.features.consultant.repository

import android.webkit.MimeTypeMap
import com.elta.android.data.features.consultant.cache.ConsultantMessageCache
import com.elta.android.data.features.consultant.datasource.WebimClient
import com.elta.android.data.features.consultant.model.WebimChat
import com.elta.android.data.features.consultant.model.toDomain
import com.elta.android.data.features.consultant.model.toDomainOwner
import com.elta.android.domain.features.consultant.model.ChatState
import com.elta.android.domain.features.consultant.model.ConnectionStatus
import com.elta.android.domain.features.consultant.model.ConsultantChat
import com.elta.android.domain.features.consultant.model.ConsultantMessage
import com.elta.android.domain.features.consultant.model.MessageOwner
import com.elta.android.domain.features.consultant.model.WebimMessageSendStatus
import com.elta.android.domain.features.consultant.model.WebimUser
import com.elta.android.domain.features.consultant.repository.ConsultantRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.webim.android.sdk.Message
import ru.webim.android.sdk.MessageStream
import ru.webim.android.sdk.MessageStream.SendFileCallback
import ru.webim.android.sdk.WebimError
import ru.webim.android.sdk.WebimSession
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ConsultantDataRepository @Inject constructor(
    private val webimClient: WebimClient,
    private val cache: ConsultantMessageCache,
    override val dispatcher: CoroutineDispatcher
) : ConsultantRepository {

    private var webimSession: WebimSession? = null

    override fun webimSessionCreate(webimUser: WebimUser, firebaseToken: String?) {
        webimSession = webimClient.sessionCreate(webimUser, firebaseToken)
    }

    override fun webimResume() {
        webimSession?.resume()
    }

    override fun webimPause() {
        webimSession?.pause()
    }

    override fun webimDestroy() {
        webimSession?.destroy()
    }

    override fun startChat() {
        webimSession?.stream?.startChat()
    }

    override suspend fun sendMessage(message: String) {
        webimSession?.stream?.sendMessage(message)
    }

    override suspend fun sendFile(file: File?): Flow<WebimMessageSendStatus> {
        val sendFlow = MutableStateFlow<WebimMessageSendStatus>(WebimMessageSendStatus.Sending)
        file?.let {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.extension)
                ?.let<String, Unit> { mimeType ->
                    webimSession?.stream?.sendFile(
                        it,
                        it.name,
                        mimeType,
                        object : SendFileCallback {
                            @Deprecated("Deprecated in Java")
                            override fun onProgress(id: Message.Id, sentBytes: Long) {
                            }

                            override fun onSuccess(id: Message.Id) {
                                sendFlow.tryEmit(WebimMessageSendStatus.Sent)
                            }

                            override fun onFailure(
                                id: Message.Id,
                                error: WebimError<SendFileCallback.SendFileError>
                            ) {
                                Timber.e("Webim send file error -> ${error.errorString}, message Id - $id, file name - ${file.name}")
                                sendFlow.tryEmit(WebimMessageSendStatus.Error(error.errorString))
                            }
                        }
                    )
                }
        }
        return sendFlow
    }

    override suspend fun sendRate(rateNumber: Int) {
        suspendCoroutine { continuation ->
            val operatorId = webimSession?.stream?.currentOperator?.id
            operatorId?.let { id ->
                webimSession?.stream?.rateOperator(
                    id,
                    rateNumber,
                    object : MessageStream.RateOperatorCallback {
                        override fun onSuccess() {
                            continuation.resume(Unit)
                        }

                        override fun onFailure(rateOperatorError: WebimError<MessageStream.RateOperatorCallback.RateOperatorError>) {
                            continuation.resumeWithException(RuntimeException(rateOperatorError.errorString))
                        }
                    })
            } ?: continuation.resumeWithException(RuntimeException("Webim operator wasn't found"))
        }
    }

    override suspend fun editMessage(id: String, newText: String) {
        suspendCoroutine { continuation ->
            val messageForEdit = cache.get(id)
            messageForEdit?.let {
                webimSession?.stream?.editMessage(
                    it,
                    newText,
                    object : MessageStream.EditMessageCallback {
                        override fun onSuccess(id: Message.Id, text: String) {
                            continuation.resume(Unit)
                        }

                        override fun onFailure(
                            id: Message.Id,
                            error: WebimError<MessageStream.EditMessageCallback.EditMessageError>
                        ) {
                            continuation.resumeWithException(RuntimeException(error.errorString))

                        }
                    })
            } ?: continuation.resumeWithException(RuntimeException("Message wasn't found"))
        }
    }

    override suspend fun deleteMessage(id: String) {
        suspendCoroutine { continuation ->
            val message = try {
                cache.get(id)
            } catch (e: Throwable) {
                null
            }
            message?.let {
                webimSession?.stream?.deleteMessage(
                    it,
                    object : MessageStream.DeleteMessageCallback {
                        override fun onSuccess(id: Message.Id) {
                            continuation.resume(Unit)
                        }

                        override fun onFailure(
                            id: Message.Id,
                            error: WebimError<MessageStream.DeleteMessageCallback.DeleteMessageError>
                        ) {
                            continuation.resumeWithException(RuntimeException(error.errorString))

                        }
                    })
            } ?: continuation.resumeWithException(RuntimeException("Message wasn't found"))
        }
    }

    override suspend fun loadLastMessages(size: Int): List<ConsultantMessage> {
        return suspendCoroutine { continuation ->
            try {
                webimClient.tracker.getLastMessages(size) { messages ->
                    messages.forEach { message ->
                        cache.put(message.serverSideId.orEmpty(), message)
                    }
                    continuation.resume(messages.toDomain(fromCache = true))
                }
            } catch (e: Throwable) {
                continuation.resume(emptyList())
            }
        }
    }

    override suspend fun loadNextCachedMessages(size: Int): List<ConsultantMessage> {
        return suspendCoroutine { continuation ->
            try {
                webimClient.tracker.getNextMessages(size) { messages ->
                    messages.forEach { message ->
                        cache.put(message.serverSideId.orEmpty(), message)
                    }
                    continuation.resume(messages.toDomain(fromCache = true))
                }
            } catch (e: Throwable) {
                continuation.resume(emptyList())
            }
        }
    }

    override fun chatState(): Flow<ChatState> =
        webimClient.webimChatState

    override fun chatNetworkStatus(): Flow<ConnectionStatus> =
        webimClient.webimNetworkStatus

    override val chat: Flow<ConsultantChat> =
        webimClient.chat
            .cacheTextUserMessage()
            .map { it.toDomain() }

    /**
     *  Кешируем оригинальные объекты Webim, потом их использовать для удаления и редактирования
     */
    private fun Flow<WebimChat>.cacheTextUserMessage() = this.onEach { messages ->
        messages.messages
            .filter { message ->
                message.type.toDomainOwner() == MessageOwner.User && message.canBeEdited()
            }
            .forEach { message ->
                cache.clear()
                message.serverSideId?.let { serverId -> cache.put(serverId, message) }
            }
    }
}
